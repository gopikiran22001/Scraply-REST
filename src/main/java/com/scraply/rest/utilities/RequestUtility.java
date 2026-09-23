package com.scraply.rest.utilities;

import com.scraply.rest.cloud.CloudinaryService;
import com.scraply.rest.common.PageResponse;
import com.scraply.rest.dto.request.NewRequestBody;
import com.scraply.rest.dto.request.RequestPickerAssignmentBody;
import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.enums.AccountStatus;
import com.scraply.rest.enums.RequestStatus;
import com.scraply.rest.enums.UserRole;
import com.scraply.rest.exception.BusinessException;
import com.scraply.rest.exception.ResourceNotFoundException;
import com.scraply.rest.exception.UnauthorizedException;
import com.scraply.rest.mapper.RequestMapper;
import com.scraply.rest.model.Request;
import com.scraply.rest.model.RequestPickerAssignment;
import com.scraply.rest.model.User;
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

@Component
@RequiredArgsConstructor
public class RequestUtility {

    private final CloudinaryService cloudinaryService;

    private final RequestRepository requestRepository;

    private final UserRepository userRepository;

    private final SecurityUtility securityUtility;

    private final RequestPickerAssignmentRepository requestPickerAssignmentRepository;

    private final RequestMapper requestMapper;


    public RequestResponse create(NewRequestBody newRequestBody) {
        String imageUrl = cloudinaryService.uploadImage(newRequestBody.getImage());

        Request request = requestMapper.toEntity(newRequestBody, imageUrl);

        requestRepository.save(request);

        return requestMapper.toResponse(request);

    }

    public RequestResponse update(RequestPickerAssignmentBody requestPickerAssignmentBody) {
        Request request = requestRepository.findById(requestPickerAssignmentBody.getRequestId())
                .orElseThrow(()->new ResourceNotFoundException("Request Not Found"));

        User picker = userRepository.findById(requestPickerAssignmentBody.getPickerId())
                .orElseThrow(()->new ResourceNotFoundException("Picker Not Found"));

        if(!AccountStatus.ACCEPTED.equals(picker.getStatus())) {
            throw new ResourceNotFoundException("Picker Not Accepted");
        }

        if(!UserRole.PICKER.equals(picker.getUserRole())) {
            throw new BusinessException("User is not a picker");
        }

        RequestPickerAssignment requestPickerAssignment = RequestPickerAssignment.builder()
                .request(request)
                .picker(picker)
                .reason(requestPickerAssignmentBody.getReason())
                .active(true)
                .build();

        if(request.getRequestPickerAssignment()!=null){
            RequestPickerAssignment requestPickerAssignment1 = request.getRequestPickerAssignment();
            requestPickerAssignment1.setActive(false);
            requestPickerAssignmentRepository.save(requestPickerAssignment1);
        }
        requestPickerAssignmentRepository.save(requestPickerAssignment);
        request.setRequestPickerAssignment(requestPickerAssignment);
        request.setRequestStatus(RequestStatus.ASSIGNED);
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
