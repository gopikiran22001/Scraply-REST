package com.scraply.rest.services;

import com.scraply.rest.cloudinary.CloudinaryService;
import com.scraply.rest.dto.*;
import com.scraply.rest.dto.auth.SignUpReq;
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
import org.springframework.web.bind.annotation.RequestBody;
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

    public User register(@RequestBody SignUpReq request) {

    }

}
