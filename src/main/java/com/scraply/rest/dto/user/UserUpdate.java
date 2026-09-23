package com.scraply.rest.dto.user;

import com.scraply.rest.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdate {

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private String phone;

    // Optional fields for picker registration
    private String address;

    private String vehicleType;

    private String vehicleNumber;

    private Integer pinCode;

    private String pickUpRoute;

    private Integer areaPinCode;

}
