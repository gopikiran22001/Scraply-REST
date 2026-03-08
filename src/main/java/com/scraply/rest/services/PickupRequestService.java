package com.scraply.rest.services;

import com.scraply.rest.models.PickupRequest;
import com.scraply.rest.models.User;
import com.scraply.rest.models.enums.Role;
import com.scraply.rest.models.enums.ScrapCategory;
import com.scraply.rest.repositories.PickupRequestRepository;
import com.scraply.rest.repositories.UserRepository;
import com.scraply.rest.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PickupRequestService {

    private final UserRepository userRepository;

    private final PickupRequestRepository pickupRequestRepository;

    public ResponseEntity<?> getPickupRequestsByCategory(ScrapCategory category) {
        String email = SecurityUtil.getCurrentUserEmail();

        User user = null;

        try {

            Optional<User> optionalUser = userRepository
                                                .findByEmail(email);

            user = optionalUser.get();
        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .build();
        }

        if (user.getRole() == Role.USER) {
            return ResponseEntity.ok(
                    pickupRequestRepository
                            .findByCategoryAndUserId(category, user.getId())
            );
        }

        if(user.getRole() == Role.PICKER) {
            return ResponseEntity.ok(
                    pickupRequestRepository
                            .findByCategoryAndPickerId(category, user.getId())
            );
        }

        if(user.getRole() == Role.ADMIN) {
            return ResponseEntity.ok(
                    pickupRequestRepository
                            .findAll()
            );
        }

        return ResponseEntity
                .notFound()
                .build();
    }

    public ResponseEntity<?> getAllPickupRequests() {
        String email = SecurityUtil.getCurrentUserEmail();

        User user = null;

        try {

            Optional<User> optionalUser = userRepository
                    .findByEmail(email);

            user = optionalUser.get();
        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .build();
        }

        if (user.getRole() == Role.USER) {
            return ResponseEntity.ok(
                    pickupRequestRepository
                            .findByUserId(user.getId())
            );
        }

        if(user.getRole() == Role.PICKER) {
            return ResponseEntity.ok(
                    pickupRequestRepository
                            .findByPickerId(user.getId())
            );
        }

        if(user.getRole() == Role.ADMIN) {
            return ResponseEntity.ok(
                    pickupRequestRepository
                            .findAll()
            );
        }

        return ResponseEntity
                .notFound()
                .build();
    }
}
