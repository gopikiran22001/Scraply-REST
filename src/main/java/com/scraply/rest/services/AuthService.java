package com.scraply.rest.services;

import com.scraply.rest.dto.*;
import com.scraply.rest.models.User;
import com.scraply.rest.models.enums.AccountStatus;
import com.scraply.rest.models.enums.AuthProvider;
import com.scraply.rest.models.enums.Role;
import com.scraply.rest.repositories.UserRepository;
import com.scraply.rest.services.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);


    public ResponseEntity<?> register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        Optional<User> existingUser = userRepository
                                        .findByEmail(request.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            log.warn("Registration failed: email already exists {}", request.getEmail());
            if(user.getRole()==Role.PICKER && user.getStatus() != AccountStatus.ACCEPTED) {
                if(user.getStatus() == AccountStatus.PENDING)
                    return ResponseEntity
                            .badRequest()
                            .body("Account Verification is Still Pending!");
                else
                    return ResponseEntity
                            .badRequest()
                            .body("Account Verification is Rejected!");
            }

            return ResponseEntity
                    .badRequest()
                    .body("Email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .provider(AuthProvider.LOCAL)
                .build();

        if(user.getRole() == null) {
            user.setRole(Role.USER); // Set default role if not provided
        }

        if(user.getRole() == Role.PICKER){
            if(request.getAddress() == null
                    || request.getVehicleType() == null
                    || request.getPickUpRoute() == null ) {
                return ResponseEntity
                        .badRequest()
                        .body("Address, vehicle type and government ID are required for picker");
            }

            user.setAddress(request.getAddress());
            user.setVehicleType(request.getVehicleType());
            user.setPickUpRoute(request.getPickUpRoute());
            user.setStatus(AccountStatus.PENDING);
        } else {
            user.setStatus(AccountStatus.ACCEPTED);
        }

        userRepository.save(user);

        log.info("User registered successfully: {}", user.getEmail());

        return ResponseEntity
                .ok("User registered successfully");
    }

    public ResponseEntity<?> mobileLogin(LoginRequest request) {
        log.info("Logging in MOBILE user with email: {}", request.getEmail());


        Optional<User> userOptional = userRepository
                                        .findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            log.warn("Login failed: user not found with email {}", request.getEmail());
            return ResponseEntity
                    .badRequest()
                    .body("Invalid credentials");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            log.warn("Login failed: invalid password for {}", request.getEmail());
            return ResponseEntity
                    .badRequest()
                    .body("Invalid credentials");
        }

        User user = userOptional.get();

        // Picker approval check
        if(user.getRole() == Role.PICKER
                && user.getStatus() != AccountStatus.ACCEPTED ) {
            return ResponseEntity.badRequest().body("Your account is not approved yet!" );
        }

        // Generate JWT
        String token = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        "",
                        java.util.List.of(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                        )
                ),
                user.getRole().name()
        );

        // Return response
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .name(user.getName())
                .build());
    }

    public ResponseEntity<?> webLogin(
            LoginRequest request,
            HttpServletResponse response ) {
        log.info("Logging in WEB user with email: {}", request.getEmail());


        Optional<User> userOptional = userRepository
                                        .findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            log.warn("Login failed: user not found with email {}", request.getEmail());
            return ResponseEntity
                    .badRequest()
                    .body("Invalid credentials");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            log.warn("Login failed: invalid password for {}", request.getEmail());
            return ResponseEntity
                    .badRequest()
                    .body("Invalid credentials");
        }

        User user = userOptional.get();

        // Picker approval check
        if(user.getRole() == Role.PICKER
                && user.getStatus() != AccountStatus.ACCEPTED ){
            return ResponseEntity
                    .badRequest()
                    .body("Your account is not approved yet!" );
        }

        // Generate JWT
        String token = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        "",
                        java.util.List.of(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                        )
                ),
                user.getRole().name()
        );

        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // use https
        cookie.setPath("/");
        cookie.setMaxAge(200 * 24 * 60 * 60);

        response.addCookie(cookie);

        // Return response
        return ResponseEntity
                .ok(AuthResponse.builder()
                .email(user.getEmail())
                .role(user.getRole().name())
                .name(user.getName())
                .build());
    }

    public ResponseEntity<?> profile(String email) {
        try {
            Optional<User> optionalUser = userRepository
                                            .findByEmail(email);

            if(optionalUser.isEmpty()) {
                return ResponseEntity
                        .notFound()
                        .build();
            }

            User user = optionalUser.get();

            return ResponseEntity.ok(ProfileResponse.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .profileImage(user.getProfileImage())
                    .role(user.getRole().name())
                    .address(user.getAddress())
                    .pickUpRoute(user.getPickUpRoute())
                    .vehicleType(user.getVehicleType())
                    .status(user.getStatus().name())
                    .build());
        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }

    public ResponseEntity<?> updateProfile(String email, ProfileUpdateRequest profileUpdateRequest ) {
        try {
            Optional<User> optionalUser = userRepository
                                            .findByEmail(email);

            if (optionalUser.isEmpty()) {
                return ResponseEntity
                        .notFound()
                        .build();
            }

            User user = optionalUser.get();

            if(profileUpdateRequest.getName() != null)
                user.setName(profileUpdateRequest.getName());

            if(profileUpdateRequest.getPhone() != null)
                user.setPhone(profileUpdateRequest.getPhone());

            if(profileUpdateRequest.getAddress() != null)
                user.setAddress(profileUpdateRequest.getAddress());

            if(profileUpdateRequest.getPickUpRoute() != null)
                user.setPickUpRoute(profileUpdateRequest.getPickUpRoute());

            if(profileUpdateRequest.getVehicleType() != null)
                user.setVehicleType(profileUpdateRequest.getVehicleType());

            if(profileUpdateRequest.getProfileImage() != null)
                user.setProfileImage(profileUpdateRequest.getProfileImage());

            if(profileUpdateRequest.getPassword() != null) {
                if(profileUpdateRequest.getPassword().trim().length() < 6)
                    return ResponseEntity
                            .badRequest()
                            .body("Password must be at least 6 characters long");
                user.setPassword(passwordEncoder.encode(profileUpdateRequest.getPassword()));
            }

            if(profileUpdateRequest.getEmail()!=null) {
                if(profileUpdateRequest.getEmail().equals(user.getEmail())) {
                    return ResponseEntity
                            .badRequest()
                            .body("New email cannot be same as old email");
                }

                Optional<User> existingUser = userRepository
                                                .findByEmail(profileUpdateRequest.getEmail());

                if(existingUser.isPresent()) {
                    return ResponseEntity
                            .badRequest()
                            .body("Email already exists");
                }

                user.setEmail(profileUpdateRequest.getEmail());
            }

            userRepository.save(user);

            return ResponseEntity
                    .ok("Profile updated successfully");

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }

}
