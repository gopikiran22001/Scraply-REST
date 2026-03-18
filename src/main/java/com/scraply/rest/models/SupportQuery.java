package com.scraply.rest.models;

import com.scraply.rest.models.enums.QueryRequestType;
import com.scraply.rest.models.enums.QueryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false)
    private String priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueryStatus status;

    @Column(length = 2000)
    private String adminResponse;

    @ManyToOne
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;

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
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
