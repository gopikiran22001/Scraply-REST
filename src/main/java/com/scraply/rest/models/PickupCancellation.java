package com.scraply.rest.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pickup_cancellation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickupCancellation {

    @Column(length = 50)
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "cancelled_by", nullable = false)
    private User cancelledBy;

    @OneToOne
    @JoinColumn(name = "pickup_request_id", nullable = false)
    private Pickup pickup;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = "PKC_" + UUID.randomUUID().toString().replace("-", "");
        }
        cancelledAt = Instant.now();
    }
}
