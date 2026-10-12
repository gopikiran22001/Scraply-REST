package com.scraply.rest.utilities;

import com.scraply.rest.cloudinary.CloudinaryService;
import com.scraply.rest.common.PageResponse;
import com.scraply.rest.dto.request.NewRequestBody;
import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.enums.AccountStatus;
import com.scraply.rest.enums.CancellationReason;
import com.scraply.rest.enums.RequestStatus;
import com.scraply.rest.enums.UserRole;
import com.scraply.rest.exception.BusinessException;
import com.scraply.rest.exception.ResourceNotFoundException;
import com.scraply.rest.exception.UnauthorizedException;
import com.scraply.rest.mapper.RequestMapper;
import com.scraply.rest.model.Request;
import com.scraply.rest.model.RequestCancellation;
import com.scraply.rest.model.RequestPickerAssignment;
import com.scraply.rest.model.User;
import com.scraply.rest.redis.RedisQueueProducer;
import com.scraply.rest.repo.RequestCancellationRepository;
import com.scraply.rest.repo.RequestPickerAssignmentRepository;
import com.scraply.rest.repo.RequestRepository;
import com.scraply.rest.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RequestUtility {

    private final CloudinaryService cloudinaryService;

    private final RequestRepository requestRepository;

    private final UserRepository userRepository;

    private final RequestCancellationRepository requestCancellationRepository;

    private final SecurityUtility securityUtility;

    private final RequestPickerAssignmentRepository requestPickerAssignmentRepository;

    private final RequestMapper requestMapper;

    private final RedisQueueProducer  redisQueueProducer;

    private void requested(Request request) {
        request.setRequestStatus(RequestStatus.REQUESTED);
        redisQueueProducer.publishRequested(request.getId());
    }

    public RequestResponse create(NewRequestBody newRequestBody) {
        String imageUrl = cloudinaryService.uploadImage(newRequestBody.getImage());

        Request request = requestMapper.toEntity(newRequestBody, imageUrl);

        requestRepository.save(request);

        redisQueueProducer.publishRequested(request.getId());

        return requestMapper.toResponse(request);

    }

    private void assignPicker(Request request, UUID pickerId, String reason) {
        User picker = userRepository.findById(pickerId)
                .orElseThrow(()->new ResourceNotFoundException("Picker Not Found"));

        if(!AccountStatus.ACCEPTED.equals(picker.getStatus())) {
            throw new ResourceNotFoundException("Picker Not Accepted");
        }

        if(!RequestStatus.IN_PROGRESS.equals(request.getRequestStatus()) &&
                !RequestStatus.ASSIGNED.equals(request.getRequestStatus())) {
            throw new BusinessException("Request Status is not Applicable to Assign");
        }

        if(!UserRole.PICKER.equals(picker.getUserRole())) {
            throw new BusinessException("User is not a picker");
        }
        if(request.getRequestPickerAssignment()!=null){
            RequestPickerAssignment requestPickerAssignment = request.getRequestPickerAssignment();
            if(requestPickerAssignment.getPicker().getId().equals(picker.getId())) {
                throw new BusinessException("Picker Assignment already exists");
            }
            requestPickerAssignment.setActive(false);
            requestPickerAssignmentRepository.save(requestPickerAssignment);
        }

        RequestPickerAssignment requestPickerAssignment = RequestPickerAssignment.builder()
                .request(request)
                .picker(picker)
                .reason(reason)
                .active(true)
                .build();
        requestPickerAssignmentRepository.save(requestPickerAssignment);
        request.setRequestPickerAssignment(requestPickerAssignment);
        request.setRequestStatus(RequestStatus.ASSIGNED);
    }

    private void cancelRequest(Request request, CancellationReason  reason, String remarks) {
        if(reason==null){
            throw new BusinessException("Reason cannot be null");
        }
        RequestCancellation requestCancellation = RequestCancellation.builder()
                .request(request)
                .cancelledBy(securityUtility.getCurrentUser())
                .reason(reason)
                .requestPickerAssignment(request.getRequestPickerAssignment())
                .remarks(remarks)
                .build();
        requestCancellationRepository.save(requestCancellation);
        if(request.getRequestPickerAssignment()!=null){
            RequestPickerAssignment requestPickerAssignment = request.getRequestPickerAssignment();
            requestPickerAssignment.setActive(false);
            requestPickerAssignmentRepository.save(requestPickerAssignment);
        }
        request.setRequestStatus(RequestStatus.CANCELLED);
    }

    private void checkPickerForRequest(Request request, UUID pickerId) {
        if(request.getRequestPickerAssignment()==null ||
                !request.getRequestPickerAssignment().isActive() ||
                !request.getRequestPickerAssignment().getPicker().getId().equals(pickerId)) {
            throw new UnauthorizedException("Unauthorized");
        }
    }

    private void completeRequest(Request request) {
        if(!RequestStatus.ASSIGNED.equals(request.getRequestStatus())) {
            throw new BusinessException("Request Status Not In Assigned");
        }
        request.setRequestStatus(RequestStatus.COMPLETED);
    }

    private void inProgressRequest(Request request) {
        if(!RequestStatus.REQUESTED.equals(request.getRequestStatus())) {
            throw new BusinessException("Request Status Not In Requested");
        }
        request.setRequestStatus(RequestStatus.IN_PROGRESS);
        redisQueueProducer.publishInProgress(request.getId());
    }

    public RequestResponse update(Request request, RequestStatus requestStatus, UUID pickerId, String reason, CancellationReason cancellationReason, String remarks) {
        UserRole userRole = securityUtility.getUserRole();
        UUID currentUserId = securityUtility.getCurrentUserId();

        if(UserRole.USER.equals(userRole)) {
            if(!currentUserId.equals(request.getUser().getId())) {
                throw new UnauthorizedException("Unauthorized");
            }
            if(!RequestStatus.IN_PROGRESS.equals(request.getRequestStatus()) &&
            !RequestStatus.REQUESTED.equals(request.getRequestStatus())) {
                throw new BusinessException("Request Status Not is Applicable to Cancel");
            }
            cancelRequest(request,cancellationReason,remarks);
        }
        else if(UserRole.PICKER.equals(userRole)) {
            checkPickerForRequest(request, currentUserId);
            switch (requestStatus) {
                case COMPLETED -> completeRequest(request);
                case CANCELLED -> cancelRequest(request, cancellationReason,remarks);
                default -> throw new BusinessException("Request Status Not Implemented");
            }
        } else {
            switch (requestStatus) {
                case IN_PROGRESS -> inProgressRequest(request);
                case ASSIGNED -> assignPicker(request, pickerId, reason);
                case CANCELLED -> cancelRequest(request,cancellationReason,remarks);
                default -> throw new BusinessException("Request Status Not Implemented");
            }
        }
        requestRepository.save(request);

        return requestMapper.toResponse(request);
    }

    public RequestResponse getRequest(Request request) {
        UUID currentUserId = securityUtility.getCurrentUserId();
        UserRole userRole = securityUtility.getUserRole();

        if(currentUserId.equals(request.getUser().getId()) ||
                UserRole.ADMIN.equals(userRole) ||
                (UserRole.PICKER.equals(userRole) && request.getRequestPickerAssignment()!=null && request.getRequestPickerAssignment().getPicker().getId().equals(currentUserId))
           ) {
            return requestMapper.toResponse(request);
        }
        throw new UnauthorizedException(
                "You do not have permission to access this request."
        );
    }

    public PageResponse<RequestResponse> getAllRequests(RequestStatus requestStatus, Integer pinCode, Instant startTime, Instant endTime, int page, int limit) {
        Pageable pageable = PageRequest.of(
                page,
                limit,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        UserRole userRole = securityUtility.getUserRole();
        UUID currentUserId = securityUtility.getCurrentUserId();

        Page<RequestResponse> requestList;

        if(UserRole.ADMIN.equals(userRole)) {
            requestList = requestRepository.findAllRequestsForAdmin(requestStatus, pinCode, startTime, endTime, pageable);
        } else if (UserRole.PICKER.equals(userRole)) {
            requestList = requestRepository.findAllRequestsByPicker(currentUserId, requestStatus, pinCode, startTime, endTime, pageable);
        } else {
            requestList = requestRepository.findAllRequestsByUser(currentUserId, requestStatus, pinCode, startTime, endTime, pageable);
        }
        return PageResponse.from(requestList);
    }
}
