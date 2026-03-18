package com.scraply.rest.repositories;

import com.scraply.rest.models.User;
import com.scraply.rest.models.enums.AccountStatus;
import com.scraply.rest.models.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    // Find all active pickers
    List<User> findByRoleAndStatus(Role role, AccountStatus status);

    // Find pickers by pinCode
    List<User> findByRoleAndStatusAndPinCode(Role role, AccountStatus status, Integer pinCode);

    // Find pickers by route
    List<User> findByRoleAndStatusAndPickUpRoute(Role role, AccountStatus status, String pickUpRoute);

    // Find pickers by nearby pinCode range (within 5 km radius, simplified as pin code nearby)
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = :status AND u.pinCode BETWEEN :minPin AND :maxPin ORDER BY u.name")
    List<User> findNearbyPickers(@Param("role") Role role, @Param("status") AccountStatus status, 
                                  @Param("minPin") Integer minPin, @Param("maxPin") Integer maxPin);
}
