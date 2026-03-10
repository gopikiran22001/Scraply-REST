package com.scraply.rest.dto;

import com.scraply.rest.models.enums.PickupStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PickupRequestUpdate {

    @NotBlank
    private Long id;

    @NotBlank
    private PickupStatus status;

    @NotBlank
    private Long assignedTo;

    private String reason;

}
