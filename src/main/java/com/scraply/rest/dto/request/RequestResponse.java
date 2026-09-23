package com.scraply.rest.dto.request;

import com.scraply.rest.enums.RequestCategory;
import com.scraply.rest.enums.RequestStatus;
import com.scraply.rest.enums.RequestType;
import lombok.*;

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
}