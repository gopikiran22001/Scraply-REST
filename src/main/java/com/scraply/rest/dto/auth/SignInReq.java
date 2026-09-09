package com.scraply.rest.dto.auth;

import lombok.Data;

@Data
public class SignInReq {
    private String email;
    private String password;
}
