package com.scraply.rest.controller;

import com.scraply.rest.common.ApiResponse;
import com.scraply.rest.dto.auth.SignInReq;
import com.scraply.rest.dto.auth.SignUpReq;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody SignUpReq request, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User Registered", authService.register(request, response)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody SignInReq request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        return ResponseEntity.ok(ApiResponse.success("User Logged In", authService.login(request, httpRequest, httpResponse)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        return ResponseEntity.ok(ApiResponse.success(authService.logout(httpRequest, httpResponse), null));
    }

}