package com.scraply.rest.mapper;

import com.scraply.rest.dto.auth.SignUpReq;
import com.scraply.rest.dto.user.PickerResponse;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.enums.AccountStatus;
import com.scraply.rest.enums.Role;
import com.scraply.rest.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    public User toEntity(SignUpReq request) {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(bCryptPasswordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .vehicleType(request.getVehicleType())
                .vehicleNumber(request.getVehicleNumber())
                .pinCode(request.getPinCode())
                .pickUpRoute(request.getPickUpRoute())
                .areaPinCode(request.getAreaPinCode())
                .role(request.getRole())
                .build();
        if (user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.PICKER))
            user.setStatus(AccountStatus.PENDING);
        return user;
    }

    public UserResponse toResponse(User user) {
        if(user.getRole().equals(Role.PICKER)) {
             return PickerResponse.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .address(user.getAddress())
                    .vehicleType(user.getVehicleType())
                    .vehicleNumber(user.getVehicleNumber())
                    .pinCode(user.getPinCode())
                    .pickUpRoute(user.getPickUpRoute())
                    .areaPinCode(user.getAreaPinCode())
                    .role(user.getRole())
                    .build();
        }

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole())
                .pinCode(user.getPinCode())
                .build();
    }
}
