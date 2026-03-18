package com.scraply.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResolveQueryRequest {

    @NotBlank
    private String adminResponse;
}
