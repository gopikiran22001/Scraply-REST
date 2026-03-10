package com.scraply.rest.dto;

import com.scraply.rest.models.enums.PickupStatus;
import com.scraply.rest.models.enums.ScrapCategory;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PickupRequestResponse {

    private Long id;

    private Long userId;

    private String userName;

    private String userPhone;

    private Long pickerId;

    private String pickerName;

    private String pickerPhone;

    private String description;

    private ScrapCategory category;

    private String imageUrl;

    // Location coordinates
    private Double latitude;

    private Double longitude;

    private String address;

    private PickupStatus status;

    private LocalDateTime requestedAt;

    private LocalDateTime assignedAt;

    private LocalDateTime completedAt;

}
