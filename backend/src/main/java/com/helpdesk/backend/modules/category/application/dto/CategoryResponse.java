package com.helpdesk.backend.modules.category.application.dto;

import com.helpdesk.backend.modules.category.domain.Category;

import java.time.LocalDateTime;

public record CategoryResponse(
        String id,
        String name,
        String description,
        String queueId,
        String queueName,
        Boolean isActive,
        LocalDateTime createdAt
) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(
                c.getId().toString(),
                c.getName(),
                c.getDescription(),
                c.getQueue() != null ? c.getQueue().getId().toString() : null,
                c.getQueue() != null ? c.getQueue().getName() : null,
                c.getIsActive(),
                c.getCreatedAt()
        );
    }
}
