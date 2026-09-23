package com.scraply.rest.service;

import com.scraply.rest.audit.annotation.Auditable;
import com.scraply.rest.dto.auth.SignInReq;
import com.scraply.rest.dto.auth.SignUpReq;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.enums.AuditAction;
import com.scraply.rest.enums.AuditEntityType;
import com.scraply.rest.utilities.UserUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserUtil userUtility;

    @Auditable(action = AuditAction.CREATE, entity = AuditEntityType.USER)
    public UserResponse register(SignUpReq request, HttpServletResponse response) {
        return userUtility.create(request, response);
    }

    @Transactional(readOnly = true)
    public UserResponse login(SignInReq request, HttpServletResponse response) {
        return userUtility.sigIn(request, response);
    }

    public String logout(HttpServletResponse response) {
        return userUtility.logout(response);
    }
}
