package com.scraply.rest.service;

import com.scraply.rest.audit.annotation.Auditable;
import com.scraply.rest.common.PageResponse;
import com.scraply.rest.dto.user.PickerDetailsUpdate;
import com.scraply.rest.dto.user.UserUpdate;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.enums.AccountStatus;
import com.scraply.rest.enums.AuditAction;
import com.scraply.rest.enums.AuditEntityType;
import com.scraply.rest.enums.UserRole;
import com.scraply.rest.exception.UnauthorizedException;
import com.scraply.rest.mapper.UserMapper;
import com.scraply.rest.model.User;
import com.scraply.rest.repo.UserRepository;
import com.scraply.rest.utilities.SecurityUtility;
import com.scraply.rest.utilities.UserUtility;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    private final UserRepository userRepository;

    private final UserUtility userUtility;

    private final SecurityUtility securityUtility;


    public UserResponse getProfile() {
        User user = userRepository.findById(securityUtility.getCurrentUserId())
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

    public PageResponse<UserResponse> getPickers(AccountStatus accountStatus, Integer pinCode, int page, int limit) {
        if (pinCode == null) {
            User user = securityUtility.getCurrentUser();
            pinCode = user.getPinCode();
        }
        return userUtility.getPickers(accountStatus, pinCode, page, limit);
    }

    @Auditable(action = AuditAction.UPDATE,entity = AuditEntityType.USER)
    @Transactional
    public UserResponse updateProfile(UserUpdate userUpdate) {
        return userUtility.updateProfile(userUpdate);
    }

    @Auditable(action = AuditAction.UPDATE,entity = AuditEntityType.USER)
    @Transactional
    public UserResponse updatePickerDetails(UUID pickerId, PickerDetailsUpdate pickerDetailsUpdate) {
        User picker = userRepository.findById(pickerId)
                .orElseThrow(() -> new RuntimeException("Picker not found"));
        if(!UserRole.PICKER.equals(picker.getUserRole())) {
            throw new UnauthorizedException("Not authorized");
        }

        return userUtility.updatePickerDetails(picker,pickerDetailsUpdate);
    }
}
