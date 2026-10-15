package com.scraply.rest.repo;

import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.enums.RequestStatus;
import com.scraply.rest.model.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID>, RequestQueryRepository {

    // ADMIN
}