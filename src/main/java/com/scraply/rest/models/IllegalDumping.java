package com.scraply.rest.models;

import com.scraply.rest.models.enums.ScrapCategory;
import com.scraply.rest.models.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "illegal_dumping_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IllegalDumping {

    @Column(length = 50)
    @Id
    private String id;


    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ScrapCategory category;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false)
    private Integer pinCode;

    private String landmark;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    @ManyToOne
    @JoinColumn(name = "assigned_picker_id")
    private User assignedPicker;

    @ManyToOne
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = true)
    private Integer priorityLevel;

    private Instant reportedAt;
    private Instant assignedAt;
    private Instant resolvedAt;


    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = "DMP_" + UUID.randomUUID().toString().replace("-", "");
        }
        reportedAt = Instant.now();
        status = Status.REQUESTED;
    }
}
