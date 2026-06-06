package com.scraply.rest.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "illegal_dumping_cancellation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IllegalDumpingCancellation {

    @Column(length = 50)
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "cancelled_by", nullable = false)
    private User cancelledBy;

    @OneToOne
    @JoinColumn(name = "illegal_dumping_id", nullable = false)
    private IllegalDumping illegalDumping;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = "DMC_" + UUID.randomUUID().toString().replace("-", "");
        }
        cancelledAt = Instant.now();
    }

}
