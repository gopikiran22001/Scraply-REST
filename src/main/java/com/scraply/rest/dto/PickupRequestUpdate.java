package com.scraply.rest.dto;

import com.scraply.rest.models.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PickupRequestUpdate {

    @NotBlank
    private String id;

    @NotNull
    private Status status;

    private String assignedTo;

    private int priorityLevel;

    private String reason;

}
