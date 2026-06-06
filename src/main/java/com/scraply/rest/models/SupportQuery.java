package com.scraply.rest.models;

import com.scraply.rest.models.enums.QueryRequestType;
import com.scraply.rest.models.enums.QueryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "support_queries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportQuery {

    @Column(length = 50)
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueryRequestType requestType;

    @Column(nullable = false)
    private String requestId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private String priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueryStatus status;

    @Column(columnDefinition = "TEXT")
    private String adminResponse;

    @ManyToOne
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant resolvedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = "QRY_" + UUID.randomUUID().toString().replace("-", "");
        }
        if (status == null) {
            status = QueryStatus.OPEN;
        }
        if (priority == null || priority.isBlank()) {
            priority = "NORMAL";
        }
        createdAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
