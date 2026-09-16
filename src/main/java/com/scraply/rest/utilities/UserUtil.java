package com.scraply.rest.utilities;

import com.scraply.rest.dto.auth.SignInReq;
import com.scraply.rest.dto.auth.SignUpReq;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.enums.AccountStatus;
import com.scraply.rest.exception.DuplicateResourceException;
import com.scraply.rest.exception.ResourceNotFoundException;
import com.scraply.rest.enums.Role;
import com.scraply.rest.mapper.UserMapper;
import com.scraply.rest.model.User;
import com.scraply.rest.repo.UserRepository;
import com.scraply.rest.security.CookieUtil;
import com.scraply.rest.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class UserUtil {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final JwtService jwtService;

    private final CookieUtil cookieUtil;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public User getCurrentUser() {
        return userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
    }

    public UUID getCurrentUserId() {
        return SecurityUtil.getCurrentUserId();
    }

    public Role getCurrentUserRole() {
        UUID id = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        return user.getRole();
    }

    public boolean isCurrentUserAdmin() {
        UUID id = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        return user.getRole().equals(Role.ADMIN);
    }

    public UserResponse create(SignUpReq request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new DuplicateResourceException("Email Already Exist");

        User user = userMapper.toEntity(request);

        userRepository.save(user);

        return userMapper.toResponse(user);
    }

    public UserResponse sigIn(SignInReq request, HttpServletResponse httpServletResponse) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        if(!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResourceNotFoundException("Invalid Credentials");
        }

        if (!user.getStatus().equals(AccountStatus.ACCEPTED)) {
            throw new ResourceNotFoundException("Account Not Accepted Yet");
        }

        cookieUtil.setTokenCookie(httpServletResponse, jwtService.generateToken(user));

        return userMapper.toResponse(user);
    }

    public String logout(HttpServletResponse response) {
        cookieUtil.clearAuthenticationCookies(response);
        return "Logged Out Successfully";
    }
}
