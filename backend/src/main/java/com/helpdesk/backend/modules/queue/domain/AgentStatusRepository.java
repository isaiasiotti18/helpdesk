package com.helpdesk.backend.modules.queue.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AgentStatusRepository extends JpaRepository<AgentStatus, UUID> {

    @Query("""
                SELECT a FROM AgentStatus a
                JOIN QueueAgent qa ON qa.agentId = a.agentId
                WHERE qa.queueId = :queueId
                AND a.status = :status
                AND a.activeTickets < a.maxTickets
                ORDER BY a.activeTickets ASC
            """)
    List<AgentStatus> findAvailableAgentsByQueue(
            @Param("queueId") UUID queueId,
            @Param("status") AgentOnlineStatus status);

    @Query("""
                SELECT a FROM AgentStatus a
                WHERE a.status = :status
                AND a.activeTickets < a.maxTickets
                ORDER BY a.activeTickets ASC
            """)
    List<AgentStatus> findAllAvailableAgents(@Param("status") AgentOnlineStatus status);
}
