package com.scraply.rest.controller;

import com.scraply.rest.common.ApiResponse;
import com.scraply.rest.common.PageResponse;
import com.scraply.rest.dto.user.PickerDetailsUpdate;
import com.scraply.rest.dto.user.PickerResponse;
import com.scraply.rest.dto.user.UserUpdate;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.enums.AccountStatus;
import com.scraply.rest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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

    @GetMapping("/get-pickers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getPickers(
            @RequestParam(defaultValue = "ACCEPTED") AccountStatus accountStatus,
            @RequestParam(required = false) Integer pinCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success("Pickers List", userService.getPickers(accountStatus, pinCode, page, limit)));
    }

    @PutMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@RequestBody UserUpdate userUpdate) {
        return ResponseEntity.ok(ApiResponse.success("User Updated", userService.updateProfile(userUpdate)));
    }

    @PutMapping("/picker-details/{pickerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> pickerDetails(@PathVariable UUID pickerId, @RequestBody PickerDetailsUpdate pickerDetailsUpdate) {
        return ResponseEntity.ok(ApiResponse.success("Picker Details Updated", userService.updatePickerDetails(pickerId, pickerDetailsUpdate)));
    }

    @PutMapping("/account-status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> pickerStatus(@RequestParam AccountStatus accountStatus, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("User Updated",userService.updateStatus(id,accountStatus)));
    }

}
