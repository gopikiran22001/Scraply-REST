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
public interface UserRepository extends JpaRepository<User, UUID>, UserQueryRepository {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(@Email(message = "Invalid email address") @NotBlank(message = "Email is required") String email);

}
