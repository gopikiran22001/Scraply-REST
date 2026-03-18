package com.scraply.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickerResponse {
    private String id;
    private String name;
    private String email;
    private String phone;
    private Integer pinCode;
    private String address;
    private String vehicleNumber;
    private String vehicleType;
    private String pickUpRoute;
    private String status;
}
