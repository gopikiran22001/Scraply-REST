package com.scraply.rest.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pickup_cancellation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickupCancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cancelled_by", nullable = false)
    private User cancelledBy;

    @OneToOne
    @JoinColumn(name = "pickup_request_id", nullable = false)
    private PickupRequest pickupRequest;

    @Column(nullable = false)
    private String reason;
}
