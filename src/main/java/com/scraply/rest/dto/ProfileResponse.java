package com.scraply.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {
    private String name;
    private String email;
    private String phone;
    private String profileImage;
    private String role;
    private String address;
    private String pickUpRoute;
    private String vehicleType;
    private String status;
}
