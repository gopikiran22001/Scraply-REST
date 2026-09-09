package com.scraply.rest.dto.user;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true) // Good practice for child DTOs using @Data
@SuperBuilder
public class PickerResponse extends UserResponse{
    private String vehicleType;

    private String vehicleNumber;

    private String pickUpRoute;

    private Integer areaPinCode;
}
