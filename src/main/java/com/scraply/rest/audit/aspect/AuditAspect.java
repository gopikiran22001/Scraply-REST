package com.scraply.rest.audit.aspect;

import com.scraply.rest.audit.annotation.Auditable;
import com.scraply.rest.audit.document.AuditLog;
import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.enums.AuditAction;
import com.scraply.rest.enums.AuditEntityType;
import com.scraply.rest.enums.AuditStatus;
import com.scraply.rest.repo.AuditLogRepository;
import com.scraply.rest.utilities.SecurityUtility;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
public class AuditAspect {
    private final AuditLogRepository auditLogRepository;

    private  final SecurityUtility securityUtility;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object result = joinPoint.proceed();
        try {
            saveAudit(auditable,AuditStatus.SUCCESS,extractEntityId(result),null);

            return result;
        } catch (Exception exception) {
            saveAudit(auditable,AuditStatus.FAILURE,extractEntityId(result),exception.getMessage());
            throw exception;
        }
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

    private void saveAudit(Auditable auditable, AuditStatus auditStatus, UUID entityId, String errorMessage) {

        AuditLog auditLog = AuditLog.builder()
                .action(auditable.action())
                .entityType(auditable.entity())
                .entityId(entityId)
                .status(auditStatus)
                .errorMessage(errorMessage)
                .ipAddress(getIpAddress())
                .userAgent(getUserAgent())
                .timestamp(Instant.now())
                .build();

        if(auditable.action().equals(AuditAction.CREATE) && auditable.entity().equals(AuditEntityType.USER)) {
            auditLog.setUserId(entityId);
        } else {
            auditLog.setUserId(securityUtility.getCurrentUserId());
        }

        auditLogRepository.save(auditLog);

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

}
