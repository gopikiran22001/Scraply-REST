package com.scraply.rest.model;

import com.scraply.rest.enums.RequestCategory;
import com.scraply.rest.enums.RequestStatus;
import com.scraply.rest.enums.RequestType;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name="requests",indexes = {
        @Index(name = "idx_request_pinCode", columnList = "pin_code"),
        @Index(name = "idx_request_status", columnList = "request_status")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Request extends BaseModel{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "request_picker_assignment_id")
    private RequestPickerAssignment requestPickerAssignment;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestCategory requestCategory;

    @Column(nullable = false,name = "request_status")
    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false,name = "pin_code")
    private Integer pinCode;

    @Column(columnDefinition = "TEXT")
    private String landMark;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(
            columnDefinition = "geography(Point, 4326)",
            nullable = false
    )
    private Point location;

}
