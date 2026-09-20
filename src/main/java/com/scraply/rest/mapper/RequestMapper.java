package com.scraply.rest.mapper;

import com.scraply.rest.dto.request.RequestBodyDTO;
import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.enums.RequestStatus;
import com.scraply.rest.model.Request;
import com.scraply.rest.utilities.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestMapper {

    private final UserUtil userUtil;

    public Request toEntity(RequestBodyDTO requestBodyDTO, String imageUrl) {
        return Request.builder()
                .requestCategory(requestBodyDTO.getRequestCategory())
                .requestType(requestBodyDTO.getRequestType())
                .imageUrl(imageUrl)
                .description(requestBodyDTO.getDescription())
                .user(userUtil.getCurrentUser())
                .address(requestBodyDTO.getAddress())
                .landMark(requestBodyDTO.getLandMark())
                .latitude(requestBodyDTO.getLatitude())
                .longitude(requestBodyDTO.getLongitude())
                .requestStatus(RequestStatus.REQUESTED)
                .build();
    }

    public RequestResponse toResponse(Request request) {
        RequestResponse requestResponse = RequestResponse.builder()
                .id(request.getId())
                .requestCategory(request.getRequestCategory())
                .requestType(request.getRequestType())
                .imageUrl(request.getImageUrl())
                .description(request.getDescription())
                .address(request.getAddress())
                .landMark(request.getLandMark())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .requestStatus(request.getRequestStatus())
                .build();
        if(request.getPicker() != null) {
            requestResponse.setPickerDetails(RequestResponse.PickerDetails.builder()
                            .id(request.getPicker().getId())
                            .firstName(request.getPicker().getFirstName())
                            .lastName(request.getPicker().getLastName())
                            .vehicleNumber(request.getPicker().getVehicleNumber())
                            .vehicleType(request.getPicker().getVehicleType())
                            .build());
        }
        return requestResponse;
    }
}
