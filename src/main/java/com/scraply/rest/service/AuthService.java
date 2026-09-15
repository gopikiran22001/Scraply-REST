package com.scraply.rest.service;

import com.scraply.rest.dto.auth.SignInReq;
import com.scraply.rest.dto.auth.SignUpReq;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.utilities.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserUtil userUtility;

    public UserResponse register(SignUpReq request) {
        return userUtility.create(request);
    }

    public UserResponse login(SignInReq request) {
        return userUtility.sigIn(request);
    }
}
