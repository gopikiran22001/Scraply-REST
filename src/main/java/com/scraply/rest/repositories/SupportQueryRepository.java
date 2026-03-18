package com.scraply.rest.repositories;

import com.scraply.rest.models.SupportQuery;
import com.scraply.rest.models.enums.QueryRequestType;
import com.scraply.rest.models.enums.QueryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportQueryRepository extends JpaRepository<SupportQuery, String> {

    List<SupportQuery> findAllByOrderByCreatedAtDesc();

    List<SupportQuery> findByStatusOrderByCreatedAtDesc(QueryStatus status);

    List<SupportQuery> findByRequestTypeOrderByCreatedAtDesc(QueryRequestType requestType);

    List<SupportQuery> findByRequestTypeAndStatusOrderByCreatedAtDesc(QueryRequestType requestType, QueryStatus status);

    List<SupportQuery> findByCreatedByIdOrderByCreatedAtDesc(String userId);

    List<SupportQuery> findByCreatedByIdAndStatusOrderByCreatedAtDesc(String userId, QueryStatus status);

    List<SupportQuery> findByCreatedByIdAndRequestTypeOrderByCreatedAtDesc(String userId, QueryRequestType requestType);

    List<SupportQuery> findByCreatedByIdAndRequestTypeAndStatusOrderByCreatedAtDesc(
            String userId,
            QueryRequestType requestType,
            QueryStatus status
    );
}
