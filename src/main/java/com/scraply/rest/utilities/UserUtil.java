package com.scraply.rest.utilities;

import com.scraply.rest.dto.auth.SignUpReq;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.exception.DuplicateResourceException;
import com.scraply.rest.exception.ResourceNotFoundException;
import com.scraply.rest.enums.Role;
import com.scraply.rest.mapper.UserMapper;
import com.scraply.rest.model.User;
import com.scraply.rest.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class UserUtil {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    public User getCurrentUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
    }

    public String getCurrentUserEmail() {
        return SecurityUtil.getCurrentUserEmail();
    }

    public UUID getCurrentUserId() {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        return user.getId();
    }

    public Role getCurrentUserRole() {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        return user.getRole();
    }

    public boolean isCurrentUserAdmin() {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        return user.getRole().equals(Role.ADMIN);
    }

    public UserResponse create(SignUpReq request) {
        if (userRepository.isExistByEmail(request.getEmail()))
            throw new DuplicateResourceException("Email Already Exist");

        User user = userMapper.toEntity(request);

        userRepository.save(user);

        return userMapper.toResponse(user);
    }
}
