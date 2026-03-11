package com.scraply.rest.models;

import com.scraply.rest.models.enums.ScrapCategory;
import com.scraply.rest.models.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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


    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private ScrapCategory category;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private String address;

    private String landmark;

    @Column(nullable = false)
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

    private LocalDateTime reportedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime resolvedAt;


    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = "DMP_" + UUID.randomUUID().toString().replace("-", "");
        }
        reportedAt = LocalDateTime.now();
        status = Status.REQUESTED;
    }
}
