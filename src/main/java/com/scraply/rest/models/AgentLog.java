package com.scraply.rest.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentLog {

    @Column(length = 50)
    @Id
    private String id;

    @Column(nullable = false)
    private String agentId;

    @Column(nullable = false, length = 20)
    private String level;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(length = 50)
    private String requestType;

    @Column(length = 80)
    private String requestId;

    @Column(length = 4000)
    private String details;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = "AGL_" + UUID.randomUUID().toString().replace("-", "");
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
