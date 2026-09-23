package com.scraply.rest.model;

import com.scraply.rest.enums.CancellationReason;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "request_cancellations",
        indexes = {
                @Index(name = "idx_cancellation_request", columnList = "request_id"),
                @Index(name = "idx_cancellation_user", columnList = "cancelled_by"),
                @Index(name = "idx_cancellation_pickerAssignment", columnList = "request_assignment_id")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestCancellation extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "request_id",
            nullable = false)
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cancelled_by",
            nullable = false
    )
    private User cancelledBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_assignment_id")
    private RequestPickerAssignment requestPickerAssignment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CancellationReason reason;

    @Column(columnDefinition = "TEXT")
    private String remarks;
}