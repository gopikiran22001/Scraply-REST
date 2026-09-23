package com.scraply.rest.dto.user;

import lombok.Data;

@Data
public class PickerDetailsUpdate {
    private String vehicleType;

    private String vehicleNumber;

    private String pickUpRoute;

    private Integer areaPinCode;
}
