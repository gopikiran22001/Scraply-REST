package com.scraply.rest.security;

import com.scraply.rest.exception.ResourceNotFoundException;
import com.scraply.rest.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UUID id  = UUID.fromString(username);
        return userRepository.findById(id)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserDetails loadUserById(UUID id) throws UsernameNotFoundException {
        return userRepository.findById(id)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

}

