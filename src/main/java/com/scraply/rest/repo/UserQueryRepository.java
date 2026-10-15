package com.scraply.rest.repo;

import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserQueryRepository {

    Page<UserResponse> findPickers(AccountStatus accountStatus, Integer pinCode, Pageable pageable);
}
