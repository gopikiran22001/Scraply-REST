package com.scraply.rest.dto.request;

import com.scraply.rest.enums.RequestCategory;
import com.scraply.rest.enums.RequestStatus;
import com.scraply.rest.enums.RequestType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestResponse {

    private UUID id;

    private RequestType requestType;

    private RequestCategory requestCategory;

    private RequestStatus requestStatus;

    private String description;

    private String imageUrl;

    private String address;

    private Integer pinCode;

    private String landMark;

    private Double latitude;

    private Double longitude;

    private Instant createdAt;

    private PickerDetails pickerDetails;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PickerDetails {

        private UUID id;

        private String firstName;

        private String lastName;

        private String vehicleNumber;

        private String vehicleType;
    }
    public RequestResponse(
            UUID id,
            RequestType requestType,
            RequestCategory requestCategory,
            RequestStatus requestStatus,
            String description,
            String imageUrl,
            String address,
            Integer pinCode,
            String landMark,
            Double latitude,
            Double longitude,
            Instant createdAt,
            UUID pickerId,
            String pickerFirstName,
            String pickerLastName,
            String vehicleNumber,
            String vehicleType
    ) {
        this.id = id;
        this.requestType = requestType;
        this.requestCategory = requestCategory;
        this.requestStatus = requestStatus;
        this.description = description;
        this.imageUrl = imageUrl;
        this.address = address;
        this.pinCode = pinCode;
        this.landMark = landMark;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = createdAt;

        if (pickerId != null) {
            this.pickerDetails = PickerDetails.builder()
                    .id(pickerId)
                    .firstName(pickerFirstName)
                    .lastName(pickerLastName)
                    .vehicleNumber(vehicleNumber)
                    .vehicleType(vehicleType)
                    .build();
        }
    }
}