package com.scraply.rest.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.scraply.rest.enums.Role;
import lombok.Data;

@Data
public class SignUpReq {

    @NotBlank(message = "FirstName is required")
    private String firstName;

    @NotBlank(message = "LastName is required")
    private String lastName;

    @Email(message = "Invalid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String phone;

    private Role role;

    // Optional fields for picker registration
    private String address;

    private String vehicleType;

    private String vehicleNumber;

    private Integer pinCode;

    private String pickUpRoute;

    private Integer areaPinCode;

}