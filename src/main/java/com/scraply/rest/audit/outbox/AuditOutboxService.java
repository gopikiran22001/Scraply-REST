package com.scraply.rest.audit.outbox;

import com.scraply.rest.audit.document.AuditLog;
import com.scraply.rest.enums.AuditAction;
import com.scraply.rest.enums.AuditEntityType;
import com.scraply.rest.enums.AuditStatus;
import com.scraply.rest.repo.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditOutboxService {

    private final AuditOutboxRepository outboxRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void enqueue(AuditAction action, AuditEntityType entityType, UUID entityId, UUID userId,
                        AuditStatus status, String errorMessage, String ipAddress,
                        String userAgent, String requestId, Instant timestamp) {
        outboxRepository.save(AuditOutbox.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .userId(userId)
                .status(status)
                .errorMessage(errorMessage)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .requestId(requestId)
                .timestamp(timestamp)
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enqueueFailure(AuditAction action, AuditEntityType entityType, UUID entityId, UUID userId,
                               AuditStatus status, String errorMessage, String ipAddress,
                               String userAgent, String requestId, Instant timestamp) {
        enqueue(action, entityType, entityId, userId, status, errorMessage, ipAddress, userAgent, requestId, timestamp);
    }

    @Scheduled(fixedDelayString = "${scraply.audit.outbox.fixed-delay:5000}")
    @Transactional
    public void publishPending() {
        for (AuditOutbox outbox : outboxRepository.findTop100ByOrderByCreatedAtAsc()) {
            try {
                auditLogRepository.save(toAuditLog(outbox));
                outboxRepository.delete(outbox);
            } catch (Exception exception) {
                log.error("Failed to publish audit outbox entry {}", outbox.getId(), exception);
            }
        }
    }

    private AuditLog toAuditLog(AuditOutbox outbox) {
        return AuditLog.builder()
                .id(outbox.getId().toString())
                .action(outbox.getAction())
                .entityType(outbox.getEntityType())
                .entityId(outbox.getEntityId())
                .userId(outbox.getUserId())
                .status(outbox.getStatus())
                .errorMessage(outbox.getErrorMessage())
                .ipAddress(outbox.getIpAddress())
                .userAgent(outbox.getUserAgent())
                .requestId(outbox.getRequestId())
                .timestamp(outbox.getTimestamp())
                .build();
    }
}
