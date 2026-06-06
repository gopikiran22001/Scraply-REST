package com.scraply.rest.dto;

import com.scraply.rest.models.enums.Status;
import com.scraply.rest.models.enums.ScrapCategory;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class PickupRequestResponse {

    private String id;

    private String userId;

    private String userName;

    private String userPhone;

    private String pickerId;

    private String pickerName;

    private String pickerPhone;

    private String description;

    private ScrapCategory category;

    private String imageUrl;

    // Location coordinates
    private Double latitude;

    private Double longitude;

    private String address;

    private int pinCode;

    private Status status;

    private int priorityLevel;

    private Instant requestedAt;

    private Instant assignedAt;

    private Instant completedAt;

    private String cancellationReason;

}
