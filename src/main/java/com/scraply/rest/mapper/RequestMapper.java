package com.scraply.rest.mapper;

import com.scraply.rest.dto.request.NewRequestBody;
import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.enums.RequestStatus;
import com.scraply.rest.model.Request;
import com.scraply.rest.utilities.UserUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestMapper {

    private final UserUtil userUtil;

    public Request toEntity(NewRequestBody newRequestBody, String imageUrl) {
        return Request.builder()
                .requestCategory(newRequestBody.getRequestCategory())
                .requestType(newRequestBody.getRequestType())
                .imageUrl(imageUrl)
                .description(newRequestBody.getDescription())
                .user(userUtil.getCurrentUser())
                .address(newRequestBody.getAddress())
                .pinCode(newRequestBody.getPinCode())
                .landMark(newRequestBody.getLandMark())
                .latitude(newRequestBody.getLatitude())
                .longitude(newRequestBody.getLongitude())
                .location(createLocation(newRequestBody.getLatitude(),newRequestBody.getLongitude()))
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
                .pinCode(request.getPinCode())
                .landMark(request.getLandMark())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .requestStatus(request.getRequestStatus())
                .build();
        if(request.getRequestPickerAssignment() != null) {
            requestResponse.setPickerDetails(RequestResponse.PickerDetails.builder()
                            .id(request.getRequestPickerAssignment().getPicker().getId())
                            .firstName(request.getRequestPickerAssignment().getPicker().getFirstName())
                            .lastName(request.getRequestPickerAssignment().getPicker().getLastName())
                            .vehicleNumber(request.getRequestPickerAssignment().getPicker().getVehicleNumber())
                            .vehicleType(request.getRequestPickerAssignment().getPicker().getVehicleType())
                            .build());
        }
        return requestResponse;
    }

    private Point createLocation(Double latitude, Double longitude) {
        GeometryFactory geometryFactory =
                new GeometryFactory(new PrecisionModel(), 4326);

        Point point = geometryFactory.createPoint(
                new Coordinate(longitude, latitude)
        );

        point.setSRID(4326);

        return point;
    }

}
