package com.scraply.rest.utilities;

import com.scraply.rest.exception.ResourceNotFoundException;
import com.scraply.rest.models.enums.Role;
import com.scraply.rest.models.User;
import com.scraply.rest.repositories.UserRepository;
import com.scraply.rest.security.SecurityUtil;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UserUtility {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public String getCurrentUserEmail() {
        return SecurityUtil.getCurrentUserEmail();
    }

    public UUID getCurrentUserId() {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return user.getId();
    }

    public Role getCurrentUserRole() {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return user.getRole();
    }

}
