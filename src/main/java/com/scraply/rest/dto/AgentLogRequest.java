package com.scraply.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentLogRequest {

    @NotBlank
    private String agentId;

    @NotBlank
    private String level;

    @NotBlank
    private String message;

    private String eventType;

    private String requestType;

    private String requestId;

    private String details;
}
