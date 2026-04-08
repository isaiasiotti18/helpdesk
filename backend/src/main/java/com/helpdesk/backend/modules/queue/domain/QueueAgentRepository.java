package com.helpdesk.backend.modules.queue.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QueueAgentRepository extends JpaRepository<QueueAgent, QueueAgentId> {
    List<QueueAgent> findByQueueId(UUID queueId);

    List<QueueAgent> findByAgentId(UUID agentId);

    boolean existsByQueueIdAndAgentId(UUID queueId, UUID agentId);

    void deleteByQueueIdAndAgentId(UUID queueId, UUID agentId);
}
