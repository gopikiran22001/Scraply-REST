package com.scraply.rest.repositories;

import com.scraply.rest.models.AgentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AgentLogRepository extends JpaRepository<AgentLog, String> {

    long countByCreatedAtAfter(Instant since);

    long countByLevelAndCreatedAtAfter(String level, Instant since);

    List<AgentLog> findTop200ByCreatedAtAfterOrderByCreatedAtDesc(Instant since);

    @Query("SELECT l.eventType, COUNT(l) FROM AgentLog l WHERE l.createdAt >= :since GROUP BY l.eventType")
    List<Object[]> countByEventTypeSince(@Param("since") Instant since);

    @Query("SELECT l.agentId, COUNT(l) FROM AgentLog l WHERE l.createdAt >= :since GROUP BY l.agentId")
    List<Object[]> countByAgentSince(@Param("since") Instant since);

    long countByLevel(String level);

    List<AgentLog> findAllByOrderByCreatedAtDesc();

    @Query("SELECT l.eventType, COUNT(l) FROM AgentLog l GROUP BY l.eventType")
    List<Object[]> countByEventType();

    @Query("SELECT l.agentId, COUNT(l) FROM AgentLog l GROUP BY l.agentId")
    List<Object[]> countByAgent();
}
