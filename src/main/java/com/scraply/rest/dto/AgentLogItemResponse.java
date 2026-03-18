package com.scraply.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentLogItemResponse {
    private String id;
    private String agentId;
    private String level;
    private String message;
    private String eventType;
    private String requestType;
    private String requestId;
    private String details;
    private LocalDateTime createdAt;
}
