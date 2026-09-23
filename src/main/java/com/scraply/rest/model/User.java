package com.scraply.rest.model;

import com.scraply.rest.enums.AccountStatus;
import com.scraply.rest.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

// 1. Base User Entity
@Entity
@Table(name = "users",indexes = {
        @Index(name = "idx_user_mail",columnList = "email"),
        @Index(name = "idx_picker_pinCode",columnList = "areaPinCode")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseModel{

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(columnDefinition = "TEXT")
    private String profileImage;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column (nullable = true)
    private Integer pinCode;

    private String vehicleNumber;
    private String vehicleType;
    private String pickUpRoute;
    private Integer areaPinCode;

    @Override
    @PrePersist
    protected void prePersist() {
        super.prePersist();
        pickerCheck();
    }

    @Override
    @PreUpdate
    protected void onUpdate() {
        super.onUpdate();
        pickerCheck();
    }

    private void pickerCheck() {
        if(this.userRole.equals(UserRole.PICKER)) {
            if (this.vehicleNumber == null || this.vehicleNumber.isBlank()) {
                throw new IllegalStateException("A Picker must have a valid vehicle number assigned.");
            }
            if (this.vehicleType == null || this.vehicleType.isBlank()) {
                throw new IllegalStateException("A Picker must have a specific vehicle type defined.");
            }
            if (this.pickUpRoute == null || this.pickUpRoute.isBlank()) {
                throw new IllegalStateException("A Picker must be assigned to an active pick-up route.");
            }
            if (this.areaPinCode == null) {
                throw new IllegalStateException("A Picker must have an assigned operating area pin code.");
            }
        }
    }

}