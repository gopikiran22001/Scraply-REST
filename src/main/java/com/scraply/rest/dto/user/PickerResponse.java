package com.scraply.rest.dto.user;

import com.scraply.rest.enums.UserRole;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PickerResponse extends UserResponse {

    private String vehicleType;

    private String vehicleNumber;

    private String pickUpRoute;

    private Integer areaPinCode;

    public PickerResponse(
            UUID id,
            String firstName,
            String lastName,
            String email,
            String phone,
            String address,
            UserRole userRole,
            Integer pinCode,
            String vehicleType,
            String vehicleNumber,
            String pickUpRoute,
            Integer areaPinCode,
            String imageUrl) {

        super(
                id,
                firstName,
                lastName,
                email,
                phone,
                address,
                userRole,
                pinCode,
                imageUrl
        );

        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
        this.pickUpRoute = pickUpRoute;
        this.areaPinCode = areaPinCode;
    }
}