package com.scraply.rest.audit.aspect;

import com.scraply.rest.audit.annotation.Auditable;
import com.scraply.rest.audit.outbox.AuditOutboxService;
import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.enums.AuditAction;
import com.scraply.rest.enums.AuditEntityType;
import com.scraply.rest.enums.AuditStatus;
import com.scraply.rest.utilities.SecurityUtility;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {
    private  final SecurityUtility securityUtility;
    private final AuditOutboxService auditOutboxService;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable exception) {
            try {
                saveAudit(auditable, AuditStatus.FAILURE, null, exception.getMessage(), true);
            } catch (Exception auditException) {
                log.error("Failed to persist failure audit for {}", auditable.action(), auditException);
            }
            throw exception;
        }

        try {
            saveAudit(auditable, AuditStatus.SUCCESS, extractEntityId(result), null, false);
        } catch (Exception auditException) {
            log.error("Failed to persist success audit for {}", auditable.action(), auditException);
        }

        return result;
    }

    private UUID extractEntityId(Object result) {

        if (result instanceof UserResponse userResponse) {
            return userResponse.getId();
        }

        if (result instanceof RequestResponse requestResponse) {
            return requestResponse.getId();
        }

        return null;
    }

    private void saveAudit(Auditable auditable, AuditStatus auditStatus, UUID entityId,
                           String errorMessage, boolean failure) {
        UUID userId;
        if(AuditAction.CREATE.equals(auditable.action()) && AuditEntityType.USER.equals(auditable.entity())) {
            userId = entityId;
        } else {
            userId = securityUtility.getCurrentUserId();
        }
        Instant timestamp = Instant.now();
        if (failure) {
            auditOutboxService.enqueueFailure(auditable.action(), auditable.entity(), entityId, userId,
                    auditStatus, errorMessage, getIpAddress(), getUserAgent(), getRequestId(), timestamp);
        } else {
            auditOutboxService.enqueue(auditable.action(), auditable.entity(), entityId, userId,
                    auditStatus, errorMessage, getIpAddress(), getUserAgent(), getRequestId(), timestamp);
        }
    }

    private String getIpAddress() {

        ServletRequestAttributes attributes =
                (ServletRequestAttributes)
                        RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return null;
        }

        HttpServletRequest request =
                attributes.getRequest();

        return request.getRemoteAddr();
    }

    private String getUserAgent() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes)
                        RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest()
                .getHeader("User-Agent");
    }

    private String getRequestId() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest().getHeader("X-Request-Id");
    }
}
