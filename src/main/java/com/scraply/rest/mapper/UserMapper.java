package com.scraply.rest.mapper;

import com.scraply.rest.cloud.CloudinaryService;
import com.scraply.rest.dto.auth.SignUpReq;
import com.scraply.rest.dto.user.PickerDetailsUpdate;
import com.scraply.rest.dto.user.PickerResponse;
import com.scraply.rest.dto.user.UserUpdate;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.enums.AccountStatus;
import com.scraply.rest.enums.UserRole;
import com.scraply.rest.exception.BusinessException;
import com.scraply.rest.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final CloudinaryService cloudinaryService;


    public User toEntity(SignUpReq request) {
        if(UserRole.AGENT.equals(request.getUserRole())) {
            throw new BusinessException("Agent is not allowed to sign up");
        }
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
                .userRole(request.getUserRole()==null?UserRole.USER:request.getUserRole())
                .build();
        if (user.getUserRole().equals(UserRole.ADMIN) || user.getUserRole().equals(UserRole.PICKER))
            user.setStatus(AccountStatus.PENDING);
        else
            user.setStatus(AccountStatus.ACCEPTED);
        return user;
    }

    public UserResponse toResponse(User user) {
        if(user.getUserRole().equals(UserRole.PICKER)) {
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
                    .userRole(user.getUserRole())
                     .profileImagerUrl(user.getProfileImage())
                    .build();
        }

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .userRole(user.getUserRole())
                .pinCode(user.getPinCode())
                .profileImagerUrl(user.getProfileImage())
                .build();
    }

    public User toEntity(User user, PickerDetailsUpdate update) {

        if (update == null) {
            return user;
        }

        if (update.getVehicleType() != null
                && !update.getVehicleType().isBlank()) {
            user.setVehicleType(update.getVehicleType());
        }

        if (update.getVehicleNumber() != null
                && !update.getVehicleNumber().isBlank()) {
            user.setVehicleNumber(update.getVehicleNumber());
        }

        if (update.getPickUpRoute() != null
                && !update.getPickUpRoute().isBlank()) {
            user.setPickUpRoute(update.getPickUpRoute());
        }

        if (update.getAreaPinCode() != null) {
            user.setAreaPinCode(update.getAreaPinCode());
        }

        return user;
    }

    public User toEntity(User user, UserUpdate userUpdate) {

        if (userUpdate.getFirstName() != null) {
            user.setFirstName(userUpdate.getFirstName());
        }

        if (userUpdate.getLastName() != null) {
            user.setLastName(userUpdate.getLastName());
        }

        if (userUpdate.getEmail() != null) {
            user.setEmail(userUpdate.getEmail());
        }

        if (userUpdate.getPhone() != null) {
            user.setPhone(userUpdate.getPhone());
        }

        if (userUpdate.getAddress() != null) {
            user.setAddress(userUpdate.getAddress());
        }

        if (userUpdate.getPinCode() != null) {
            user.setPinCode(userUpdate.getPinCode());
        }

        if(userUpdate.getProfileImage()!=null){
            String imageUrl = cloudinaryService.uploadImage(userUpdate.getProfileImage(),"scraply/profile");
            user.setProfileImage(imageUrl);
        }

        return user;
    }
}
