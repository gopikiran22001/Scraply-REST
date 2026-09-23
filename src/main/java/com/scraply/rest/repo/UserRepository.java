package com.scraply.rest.repo;

import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.enums.AccountStatus;
import com.scraply.rest.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(@Email(message = "Invalid email address") @NotBlank(message = "Email is required") String email);

    @Query("""
        SELECT new com.scraply.rest.dto.user.PickerResponse(
            u.id,
            u.firstName,
            u.lastName,
            u.email,
            u.phone,
            u.address,
            u.userRole,
            u.pinCode,
            u.vehicleType,
            u.vehicleNumber,
            u.pickUpRoute,
            u.areaPinCode
        )
        FROM User u
        WHERE u.userRole = UserRole.PICKER
          AND u.status = :accountStatus
          AND (:pinCode IS NULL OR u.pinCode = :pinCode)
    """)
    Page<UserResponse> findPickers(
            @Param("accountStatus") AccountStatus accountStatus,
            @Param("pinCode") Integer pinCode,
            Pageable pageable
    );
}
