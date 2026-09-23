package com.scraply.rest.service;

import com.scraply.rest.audit.annotation.Auditable;
import com.scraply.rest.common.PageResponse;
import com.scraply.rest.dto.user.PickerResponse;
import com.scraply.rest.dto.user.UpdateUser;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.enums.AccountStatus;
import com.scraply.rest.enums.AuditAction;
import com.scraply.rest.enums.AuditEntityType;
import com.scraply.rest.mapper.UserMapper;
import com.scraply.rest.model.User;
import com.scraply.rest.repo.UserRepository;
import com.scraply.rest.utilities.SecurityUtil;
import com.scraply.rest.utilities.UserUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    private final UserRepository userRepository;

    private final UserUtil userUtility;


    public UserResponse getProfile() {
        User user = userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toResponse(user);
    }

    @Auditable(action = AuditAction.STATUS_CHANGE,entity = AuditEntityType.USER)
    @Transactional
    public UserResponse updateStatus(UUID id, AccountStatus accountStatus) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userUtility.setAccountStatus(user,accountStatus);

    }

    public PageResponse<PickerResponse> getPickers(AccountStatus accountStatus, Integer pinCode, int page, int limit) {
        if (pinCode == null) {
            User user = userUtility.getCurrentUser();
            pinCode = user.getPinCode();
        }
        return userUtility.getPickers(accountStatus, pinCode, page, limit);
    }

    @Auditable(action = AuditAction.UPDATE,entity = AuditEntityType.USER)
    @Transactional
    public UserResponse updateProfile(UpdateUser updateUser) {
        User user = userUtility.getCurrentUser();

        user = userMapper.toEntity(user, updateUser);

        userRepository.save(user);

        return userMapper.toResponse(user);
    }
}
