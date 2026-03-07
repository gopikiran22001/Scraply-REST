package com.scraply.rest.controllers;

import com.scraply.rest.dto.LoginRequest;
import com.scraply.rest.dto.ProfileUpdateRequest;
import com.scraply.rest.dto.RegisterRequest;
import com.scraply.rest.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController()
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final AuthService authService;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        String email = authentication.getName();
        return authService.profile(email);
    }

    @PutMapping("/")
    public ResponseEntity<?> updateProfile(Authentication authentication,@RequestBody ProfileUpdateRequest profileUpdateRequest) {
        String email = authentication.getName();
        return authService.updateProfile(email,profileUpdateRequest);
    }

}
