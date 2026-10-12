package com.scraply.rest.service;

import com.scraply.rest.audit.annotation.Auditable;
import com.scraply.rest.dto.auth.SignInReq;
import com.scraply.rest.dto.auth.SignUpReq;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.enums.AuditAction;
import com.scraply.rest.enums.AuditEntityType;
import com.scraply.rest.utilities.UserUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserUtility userUtility;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @Auditable(action = AuditAction.CREATE, entity = AuditEntityType.USER)
    public UserResponse register(SignUpReq request, HttpServletResponse response) {
        return userUtility.create(request, response);
    }

    @Transactional(readOnly = true)
    public UserResponse login(SignInReq request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        UserResponse userResponse = userUtility.sigIn(request, httpResponse);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        new ChangeSessionIdAuthenticationStrategy().onAuthentication(authentication, httpRequest, httpResponse);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return userResponse;
    }

    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return userUtility.logout(response);
    }
}

