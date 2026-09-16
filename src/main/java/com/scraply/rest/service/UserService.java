package com.scraply.rest.service;

import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.mapper.UserMapper;
import com.scraply.rest.model.User;
import com.scraply.rest.repo.UserRepository;
import com.scraply.rest.utilities.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    private final UserRepository userRepository;


    public UserResponse getProfile() {
        User user = userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toResponse(user);
    }

}
