package com.scraply.rest.models;

import com.scraply.rest.models.enums.PickupStatus;
import com.scraply.rest.models.enums.ScrapCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pickup_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickupRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User who created the request
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Picker assigned to collect scrap
    @ManyToOne
    @JoinColumn(name = "picker_id")
    private User picker;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private ScrapCategory category;

    private String imageUrl;

    // Location coordinates
    private Double latitude;

    private Double longitude;

    private String address;

    @Enumerated(EnumType.STRING)
    private PickupStatus status;

    private LocalDateTime requestedAt;

    private LocalDateTime assignedAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
        status = PickupStatus.REQUESTED;
    }
}