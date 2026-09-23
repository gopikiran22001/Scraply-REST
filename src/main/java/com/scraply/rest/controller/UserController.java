package com.scraply.rest.controller;

import com.scraply.rest.common.ApiResponse;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.enums.AccountStatus;
import com.scraply.rest.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> user() {
        return ResponseEntity.ok(ApiResponse.success("User Info", userService.getProfile()));
    }

    @PutMapping("/account-status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> pickerStatus(@RequestParam AccountStatus accountStatus, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("User Updated",userService.updateStatus(id,accountStatus)));
    }

}
