package com.scraply.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebAuthResponse {

    private String name;
    private String email;
    private String role;

}