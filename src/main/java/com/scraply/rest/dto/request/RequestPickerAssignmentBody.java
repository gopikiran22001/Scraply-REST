package com.scraply.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestPickerAssignmentBody {

    @NotNull
    private UUID requestId;

    @NotNull
    private UUID pickerId;

    private String reason;
}
