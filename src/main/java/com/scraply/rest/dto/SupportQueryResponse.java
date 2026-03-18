package com.scraply.rest.dto;

import com.scraply.rest.models.enums.QueryRequestType;
import com.scraply.rest.models.enums.QueryStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SupportQueryResponse {

    private String id;

    private QueryRequestType requestType;

    private String requestId;

    private String subject;

    private String message;

    private String priority;

    private QueryStatus status;

    private String adminResponse;

    private String createdById;

    private String createdByName;

    private String resolvedById;

    private String resolvedByName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;
}
