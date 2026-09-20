package com.scraply.rest.dto.user;

import com.scraply.rest.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String address;

    private UserRole userRole;

    private Integer pinCode;

}
