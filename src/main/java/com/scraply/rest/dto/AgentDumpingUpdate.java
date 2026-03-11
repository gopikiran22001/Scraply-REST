package com.scraply.rest.dto;

import com.scraply.rest.models.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AgentDumpingUpdate {

    @NotNull
    String agentId;

    @NotNull
    String dumpingId;

    @NotNull
    Status status;

    String reason;

    String pickerId;
}
