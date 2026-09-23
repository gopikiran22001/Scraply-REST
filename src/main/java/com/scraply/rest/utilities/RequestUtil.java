package com.scraply.rest.utilities;

import com.scraply.rest.cloud.CloudinaryService;
import com.scraply.rest.dto.request.NewRequestBody;
import com.scraply.rest.dto.request.RequestPickerAssignmentBody;
import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.enums.AccountStatus;
import com.scraply.rest.exception.ResourceNotFoundException;
import com.scraply.rest.mapper.RequestMapper;
import com.scraply.rest.model.Request;
import com.scraply.rest.model.RequestPickerAssignment;
import com.scraply.rest.model.User;
import com.scraply.rest.repo.RequestPickerAssignmentRepository;
import com.scraply.rest.repo.RequestRepository;
import com.scraply.rest.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestUtil {

    private final CloudinaryService cloudinaryService;

    private final RequestRepository requestRepository;

    private final UserRepository userRepository;

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
        requestRepository.save(request);


        return requestMapper.toResponse(request);
    }
}
