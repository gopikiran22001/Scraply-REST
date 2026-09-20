package com.scraply.rest.audit.aspect;

import com.scraply.rest.audit.annotation.Auditable;
import com.scraply.rest.audit.document.AuditLog;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.enums.AuditAction;
import com.scraply.rest.enums.AuditEntityType;
import com.scraply.rest.enums.AuditStatus;
import com.scraply.rest.repo.AuditLogRepository;
import com.scraply.rest.utilities.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
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

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        try {
            Object result = joinPoint.proceed();

            saveAudit(auditable,AuditStatus.SUCCESS,null);

            return result;
        } catch (Exception exception) {
            saveAudit(auditable,AuditStatus.FAILURE,exception.getMessage());
            throw exception;
        }
    }

    @AfterReturning(
            pointcut =
                    "execution(* com.scraply.rest.service.AuthService.register(..)) || " +
                    "execution(* com.scraply.rest.service.RequestService.create(..))",
            returning = "result"
    )
    public void afterReturning(
            JoinPoint joinPoint,
            Object result
    ) {

        if (result instanceof UserResponse userResponse) {

            AuditLog auditLog = AuditLog.builder()
                    .userId(userResponse.getId())
                    .action(AuditAction.CREATE)
                    .entityType(AuditEntityType.USER)
                    .status(AuditStatus.SUCCESS)
                    .timestamp(Instant.now())
                    .build();

            auditLogRepository.save(auditLog);
        }
    }



    private void saveAudit(Auditable auditable, AuditStatus auditStatus, String errorMessage) {

        AuditLog auditLog = AuditLog.builder()
                .userId(SecurityUtil.getCurrentUserId())
                .action(auditable.action())
                .entityType(auditable.entity())
                .entityId(UUID.fromString(auditable.entityId()))
                .status(auditStatus)
                .errorMessage(errorMessage)
                .ipAddress(getIpAddress())
                .userAgent(getUserAgent())
                .timestamp(Instant.now())
                .build();

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
