package com.scraply.rest.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "request_picker_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestPickerAssignment extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "picker_id", nullable = false)
    private User picker;

    @Column(nullable = false)
    private boolean active;

    @Column(columnDefinition = "TEXT")
    private String reason;
}