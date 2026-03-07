package com.scraply.rest.dto;

import com.scraply.rest.models.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String name;

    private String email;

    private String password;

    private String phone;

    private String profileImage;

    // Optional fields for picker registration
    private String address;

    private String vehicleType;

    private String pickUpRoute;

}
