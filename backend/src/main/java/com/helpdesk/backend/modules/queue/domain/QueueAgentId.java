package com.helpdesk.backend.modules.queue.domain;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class QueueAgentId implements Serializable {
    private UUID queueId;
    private UUID agentId;
}
