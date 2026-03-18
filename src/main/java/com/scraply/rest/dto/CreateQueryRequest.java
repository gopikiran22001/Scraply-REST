package com.scraply.rest.dto;

import com.scraply.rest.models.enums.QueryRequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateQueryRequest {

    @NotNull
    private QueryRequestType requestType;

    @NotBlank
    private String requestId;

    @NotBlank
    private String subject;

    @NotBlank
    private String message;

    private String priority;
}
