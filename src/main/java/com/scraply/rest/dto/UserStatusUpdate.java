package com.scraply.rest.dto;

import com.scraply.rest.models.enums.AccountStatus;
import com.scraply.rest.models.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdate {
    @NotNull
    private String userId;

    @NotNull
    private AccountStatus status;
}
