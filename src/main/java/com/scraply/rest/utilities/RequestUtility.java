package com.scraply.rest.utilities;

import com.scraply.rest.cloud.CloudinaryService;
import com.scraply.rest.common.PageResponse;
import com.scraply.rest.dto.request.NewRequestBody;
import com.scraply.rest.dto.request.RequestPickerAssignmentBody;
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

import java.nio.file.AccessDeniedException;
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


    public RequestResponse create(NewRequestBody newRequestBody) {
        String imageUrl = cloudinaryService.uploadImage(newRequestBody.getImage());

        Request request = requestMapper.toEntity(newRequestBody, imageUrl);

        requestRepository.save(request);

        return requestMapper.toResponse(request);

    }

    private void assignPicker(Request request, UUID pickerId, String reason) {
        User picker = userRepository.findById(pickerId)
                .orElseThrow(()->new ResourceNotFoundException("Picker Not Found"));

        if(!AccountStatus.ACCEPTED.equals(picker.getStatus())) {
            throw new ResourceNotFoundException("Picker Not Accepted");
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

    private void checkPicker(Request request, User picker) {
        if(request.getRequestPickerAssignment()==null ||
                !request.getRequestPickerAssignment().isActive() ||
                !request.getRequestPickerAssignment().getPicker().getId().equals(picker.getId())) {
            throw new UnauthorizedException("Unauthorized");
        }
    }

    public RequestResponse update(Request request, RequestStatus requestStatus, UUID pickerId, String reason, CancellationReason cancellationReason, String remarks) {
        User user = securityUtility.getCurrentUser();
        if(UserRole.PICKER.equals(user.getUserRole())) {
            checkPicker(request,user);
            switch (requestStatus) {
                case COMPLETED -> request.setRequestStatus(RequestStatus.COMPLETED);
                case CANCELLED -> cancelRequest(request, cancellationReason,remarks);
                default -> throw new BusinessException("Request Status Not Implemented");
            }
        } else {
            switch (requestStatus) {
                case IN_PROGRESS -> request.setRequestStatus(RequestStatus.IN_PROGRESS);
                case ASSIGNED -> assignPicker(request, pickerId, reason);
                case COMPLETED -> request.setRequestStatus(RequestStatus.COMPLETED);
                case CANCELLED -> cancelRequest(request,cancellationReason,remarks);
                default -> throw new BusinessException("Request Status Not Implemented");
            }
        }
        requestRepository.save(request);

        return requestMapper.toResponse(request);
    }

    public RequestResponse getRequest(Request request) {
        User user = securityUtility.getCurrentUser();

        if(user.getId().equals(request.getUser().getId()) ||
                UserRole.ADMIN.equals(user.getUserRole()) ||
                (UserRole.PICKER.equals(user.getUserRole()) && request.getRequestPickerAssignment()!=null && request.getRequestPickerAssignment().getPicker().getId().equals(user.getId()))
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

        User user = securityUtility.getCurrentUser();

        Page<RequestResponse> requestList ;

        if(UserRole.ADMIN.equals(user.getUserRole())) {
            requestList = requestRepository.findAllRequestsForAdmin(requestStatus, pinCode, startTime, endTime, pageable);
        } else if (UserRole.PICKER.equals(user.getUserRole())) {
            requestList = requestRepository.findAllRequestsByPicker(user.getId(), requestStatus, pinCode, startTime, endTime, pageable);
        } else {
            requestList = requestRepository.findAllRequestsByUser(user.getId(), requestStatus, pinCode, startTime, endTime, pageable);
        }
        return PageResponse.from(requestList);
    }
}
