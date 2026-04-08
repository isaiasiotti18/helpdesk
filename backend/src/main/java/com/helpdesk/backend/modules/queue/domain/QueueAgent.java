package com.helpdesk.backend.modules.queue.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "queue_agents")
@IdClass(QueueAgentId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueAgent {

    @Id
    @Column(name = "queue_id")
    private UUID queueId;

    @Id
    @Column(name = "agent_id")
    private UUID agentId;

    @Column(name = "joined_at", nullable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();
}
