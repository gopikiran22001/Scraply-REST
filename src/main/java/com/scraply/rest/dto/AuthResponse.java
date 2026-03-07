package com.scraply.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String name;
    private String email;
    private String role;
    private String token;

}