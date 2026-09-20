package com.scraply.rest.model;

import com.scraply.rest.enums.RequestCategory;
import com.scraply.rest.enums.RequestStatus;
import com.scraply.rest.enums.RequestType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Request extends BaseModel{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "picker_id")
    private User picker;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestCategory requestCategory;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(columnDefinition = "TEXT")
    private String landMark;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

}
