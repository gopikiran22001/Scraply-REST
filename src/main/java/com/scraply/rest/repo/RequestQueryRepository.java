package com.scraply.rest.repo;

import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface RequestQueryRepository {

    Page<RequestResponse> findAllRequestsForAdmin(
            RequestStatus requestStatus,
            Integer pinCode,
            Instant startTime,
            Instant endTime,
            Pageable pageable
    );

    Page<RequestResponse> findAllRequestsByUser(
            UUID userId,
            RequestStatus requestStatus,
            Integer pinCode,
            Instant startTime,
            Instant endTime,
            Pageable pageable
    );

    Page<RequestResponse> findAllRequestsByPicker(
            UUID userId,
            RequestStatus requestStatus,
            Integer pinCode,
            Instant startTime,
            Instant endTime,
            Pageable pageable
    );
}
