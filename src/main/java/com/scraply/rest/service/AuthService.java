package com.scraply.rest.service;

import com.scraply.rest.dto.auth.SignInReq;
import com.scraply.rest.dto.auth.SignUpReq;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.utilities.UserUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserUtility userUtility;

    public UserResponse register(SignUpReq request) {
        return userUtility.create(request);
    }

    public UserResponse login(SignInReq request) {
    }
}
