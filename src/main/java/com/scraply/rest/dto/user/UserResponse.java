package com.scraply.rest.dto.user;

import com.scraply.rest.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.N;

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

    private Role role;

    private Integer pinCode;

}
