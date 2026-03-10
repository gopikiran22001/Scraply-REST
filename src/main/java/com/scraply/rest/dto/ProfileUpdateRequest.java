package com.scraply.rest.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfileUpdateRequest {
    private String name;

    private String email;

    private String password;

    private String phone;

    private MultipartFile image;

    // Optional fields for picker registration
    private String address;

    private String vehicleType;

    private String pickUpRoute;

}
