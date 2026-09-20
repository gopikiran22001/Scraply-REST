package com.scraply.rest.controller;

import com.scraply.rest.common.ApiResponse;
import com.scraply.rest.dto.auth.SignInReq;
import com.scraply.rest.dto.auth.SignUpReq;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody SignUpReq request, HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.success("User Registered", authService.register(request, response)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@RequestBody SignInReq request, HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.success("User Logged In", authService.login(request,response)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.success( authService.logout(response), null));
    }

}