package com.scraply.rest.controllers;

import com.scraply.rest.dto.LoginRequest;
import com.scraply.rest.dto.ProfileUpdateRequest;
import com.scraply.rest.dto.RegisterRequest;
import com.scraply.rest.dto.UserStatusUpdate;
import com.scraply.rest.services.AgentService;
import com.scraply.rest.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final AgentService agentService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
        ) {

        String clientType = httpRequest.getHeader("X-Platform");

        if ("MOBILE".equalsIgnoreCase(clientType)) {
            return ResponseEntity.ok(authService.mobileLogin(request));
        } else {
            return ResponseEntity.ok(authService.webLogin(request, response));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {

        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true in production
        cookie.setPath("/");
        cookie.setMaxAge(0); // delete cookie

        response.addCookie(cookie);

        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok(authService.profile());
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            @ModelAttribute ProfileUpdateRequest profileUpdateRequest
        ) {
        log.debug("Update profile request: {}", profileUpdateRequest);
        return ResponseEntity.ok(authService.updateProfile(profileUpdateRequest));
    }

    @PutMapping("/status/update")
    public ResponseEntity<?> userStatusUpdate(@RequestBody UserStatusUpdate userStatusUpdate) {
        return ResponseEntity.ok(authService.userStatusUpdate(userStatusUpdate));
    }

    @GetMapping("/pickers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllPickers() {
        return ResponseEntity.ok(authService.getAllPickers());
    }

    @GetMapping("/pickers/pincode/{pinCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPickersByPinCode(@PathVariable Integer pinCode) {
        return ResponseEntity.ok(authService.getPickersByPinCode(pinCode));
    }

    @GetMapping("/pickers/route/{route}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPickersByRoute(@PathVariable String route) {
        return ResponseEntity.ok(authService.getPickersByRoute(route));
    }

    @GetMapping("/pickers/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllPickersAnyStatus() {
        return ResponseEntity.ok(authService.getAllPickersAnyStatus());
    }

    @GetMapping("/agent-logs/report")
    public ResponseEntity<?> getAgentLogReport(
            @RequestParam(required = false) Integer hours,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(agentService.getAgentLogReport(hours, limit));
    }

}
