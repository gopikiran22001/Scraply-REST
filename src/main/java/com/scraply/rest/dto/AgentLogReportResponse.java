package com.scraply.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentLogReportResponse {
    private Integer periodHours;
    private Long totalLogs;
    private Long errorLogs;
    private Long warningLogs;
    private Map<String, Long> logsByEventType;
    private Map<String, Long> logsByAgent;
    private List<AgentLogItemResponse> recentLogs;
}
