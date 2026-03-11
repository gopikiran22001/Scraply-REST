package com.scraply.rest.models;

import com.scraply.rest.models.enums.Status;
import com.scraply.rest.models.enums.ScrapCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pickup_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pickup {

    @Column(length = 50)
    @Id
    private String id;

    // User who created the request
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Picker assigned to collect scrap
    @ManyToOne
    @JoinColumn(name = "picker_id")
    private User picker;

    @ManyToOne
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

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
    private Status status;

    private LocalDateTime requestedAt;

    private LocalDateTime assignedAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = "PKP_" + UUID.randomUUID().toString().replace("-", "");
        }
        requestedAt = LocalDateTime.now();
        status = Status.REQUESTED;
    }

}