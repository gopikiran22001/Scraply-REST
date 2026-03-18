package com.scraply.rest.repositories;

import com.scraply.rest.models.AgentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AgentLogRepository extends JpaRepository<AgentLog, String> {

    long countByCreatedAtAfter(LocalDateTime since);

    long countByLevelAndCreatedAtAfter(String level, LocalDateTime since);

    List<AgentLog> findTop200ByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime since);

    @Query("SELECT l.eventType, COUNT(l) FROM AgentLog l WHERE l.createdAt >= :since GROUP BY l.eventType")
    List<Object[]> countByEventTypeSince(@Param("since") LocalDateTime since);

    @Query("SELECT l.agentId, COUNT(l) FROM AgentLog l WHERE l.createdAt >= :since GROUP BY l.agentId")
    List<Object[]> countByAgentSince(@Param("since") LocalDateTime since);
}
