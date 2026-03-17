package com.scraply.rest.dto;

import com.scraply.rest.models.enums.ScrapCategory;
import com.scraply.rest.models.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class IllegalDumpingResponse {
    private String id;

    private String userId;

    private String userName;

    private String userPhone;

    private String pickerId;

    private String pickerName;

    private String pickerPhone;

    private String description;

    private ScrapCategory category;

    private String landmark;

    private String imageUrl;

    // Location coordinates
    private Double latitude;

    private Double longitude;

    private String address;

    private Integer pinCode;

    private Status status;

    private Integer priorityLevel;

    private LocalDateTime reportedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime resolvedAt;
}
