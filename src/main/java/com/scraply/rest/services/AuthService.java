package com.scraply.rest.services;

import com.scraply.rest.cloudinary.CloudinaryService;
import com.scraply.rest.dto.*;
import com.scraply.rest.exception.BadRequestException;
import com.scraply.rest.exception.ResourceNotFoundException;
import com.scraply.rest.models.User;
import com.scraply.rest.models.enums.AccountStatus;
import com.scraply.rest.models.enums.AuthProvider;
import com.scraply.rest.models.enums.Role;
import com.scraply.rest.repositories.UserRepository;
import com.scraply.rest.security.SecurityUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    private User getCurrentUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public String register(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
            log.warn("Registration failed: email already exists {}", request.getEmail());
            if (existingUser.getRole() == Role.PICKER && existingUser.getStatus() != AccountStatus.ACCEPTED) {
                if (existingUser.getStatus() == AccountStatus.PENDING) {
                    throw new BadRequestException("Account Verification is Still Pending!");
                } else {
                    throw new BadRequestException("Account Verification is Rejected!");
                }
            }
            throw new BadRequestException("Email already exists");
        });

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .provider(AuthProvider.LOCAL)
                .build();

        if (user.getRole() == null || user.getRole() == Role.USER) {
            user.setRole(Role.USER);
            user.setStatus(AccountStatus.ACCEPTED);
        } else if (user.getRole() == Role.PICKER) {
            if (request.getAddress() == null
                    || request.getVehicleType() == null
                    || request.getPickUpRoute() == null
                    || request.getPinCode() == null
                    || request.getVehicleNumber() == null) {
                throw new BadRequestException("Address, vehicle type, vehicle number, pincode and pickup route are required for picker");
            }
            user.setAddress(request.getAddress());
            user.setVehicleType(request.getVehicleType());
            user.setPickUpRoute(request.getPickUpRoute());
            user.setPinCode(request.getPinCode());
            user.setVehicleNumber(request.getVehicleNumber());
            user.setStatus(AccountStatus.PENDING);
        } else if (user.getRole() == Role.ADMIN) {
            user.setStatus(AccountStatus.PENDING);
        } else {
            throw new BadRequestException("Invalid ROLE");
        }

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());
        return "User registered successfully";
    }

    public MobileAuthResponse mobileLogin(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found with email {}", request.getEmail());
                    return new BadCredentialsException("Invalid credentials");
                });

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            log.warn("Login failed: invalid password for {}", request.getEmail());
            throw new BadCredentialsException("Invalid credentials");
        }

        if (user.getRole() == Role.PICKER && user.getStatus() != AccountStatus.ACCEPTED) {
            throw new BadRequestException("Your account is not approved yet!");
        }

        if(user.getRole() == Role.AGENT) {
            throw new BadRequestException("This account can't be logged");
        }

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

        return MobileAuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .name(user.getName())
                .build();
    }

    public WebAuthResponse webLogin(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found with email {}", request.getEmail());
                    return new BadCredentialsException("Invalid credentials");
                });

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            log.warn("Login failed: invalid password for {}", request.getEmail());
            throw new BadCredentialsException("Invalid credentials");
        }

        if (user.getRole() == Role.PICKER && user.getStatus() != AccountStatus.ACCEPTED) {
            throw new BadRequestException("Your account is not approved yet!");
        }

        if(user.getRole() == Role.AGENT) {
            throw new BadRequestException("This account can't be logged");
        }

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
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(200 * 24 * 60 * 60);
        response.addCookie(cookie);

        return WebAuthResponse.builder()
                .email(user.getEmail())
                .role(user.getRole().name())
                .name(user.getName())
                .build();
    }

    public ProfileResponse profile() {
        User user = getCurrentUser();

        return ProfileResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .role(user.getRole().name())
                .address(user.getAddress())
                .pickUpRoute(user.getPickUpRoute())
                .vehicleType(user.getVehicleType())
                .status(user.getStatus().name())
                .build();
    }

    public String updateProfile(ProfileUpdateRequest profileUpdateRequest) {
        User user = getCurrentUser();

        if (profileUpdateRequest.getName() != null)
            user.setName(profileUpdateRequest.getName());

        if (profileUpdateRequest.getPhone() != null)
            user.setPhone(profileUpdateRequest.getPhone());

        if (profileUpdateRequest.getAddress() != null)
            user.setAddress(profileUpdateRequest.getAddress());

        if (profileUpdateRequest.getPickUpRoute() != null)
            user.setPickUpRoute(profileUpdateRequest.getPickUpRoute());

        if (profileUpdateRequest.getVehicleType() != null)
            user.setVehicleType(profileUpdateRequest.getVehicleType());

        if (profileUpdateRequest.getPassword() != null) {
            if (profileUpdateRequest.getPassword().trim().length() < 6) {
                throw new BadRequestException("Password must be at least 6 characters long");
            }
            user.setPassword(passwordEncoder.encode(profileUpdateRequest.getPassword()));
        }

        if (profileUpdateRequest.getEmail() != null &&
                !profileUpdateRequest.getEmail().equals(user.getEmail())) {

            userRepository.findByEmail(profileUpdateRequest.getEmail()).ifPresent(existingUser -> {
                throw new BadRequestException("Email already exists");
            });

            user.setEmail(profileUpdateRequest.getEmail());
        }


        // Upload image to Cloudinary and save the URL
        if (profileUpdateRequest.getImage() != null && !profileUpdateRequest.getImage().isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(profileUpdateRequest.getImage());
            user.setProfileImage(imageUrl);
        }

        userRepository.save(user);
        return "Profile updated successfully";
    }

    public Object userStatusUpdate(UserStatusUpdate userStatusUpdate) {
        User user = getCurrentUser();
        if(user.getRole() != Role.ADMIN)
            throw new BadRequestException("Unauthorized to update user status");
        User userToUpdate = userRepository.findById(userStatusUpdate.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userStatusUpdate.getUserId()));

        userToUpdate.setStatus(userStatusUpdate.getStatus());
        if(userStatusUpdate.getStatus() == AccountStatus.ACCEPTED) {
            userToUpdate.setApprovedBy(user);
        }
        userRepository.save(userToUpdate);
        return "Status updated successfully";
    }
}
