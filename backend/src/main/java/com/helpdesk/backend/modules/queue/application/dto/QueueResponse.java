package com.helpdesk.backend.modules.queue.application.dto;

import com.helpdesk.backend.modules.queue.domain.Queue;

import java.time.LocalDateTime;

public record QueueResponse(
        String id,
        String name,
        String description,
        Integer maxAgents,
        Boolean isActive,
        LocalDateTime createdAt) {
    public static QueueResponse from(Queue queue) {
        return new QueueResponse(
                queue.getId().toString(),
                queue.getName(),
                queue.getDescription(),
                queue.getMaxAgents(),
                queue.getIsActive(),
                queue.getCreatedAt());
    }
}
