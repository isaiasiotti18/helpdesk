package com.helpdesk.backend.modules.ticket.application.dtos;

import com.helpdesk.backend.modules.ticket.domain.Ticket;

import java.time.LocalDateTime;

public record SearchResultResponse(
        String id,
        String title,
        String description,
        String status,
        String priority,
        String createdByName,
        String assignedAgentName,
        String categoryName,
        LocalDateTime createdAt
) {
    public static SearchResultResponse from(Ticket t) {
        return new SearchResultResponse(
                t.getId().toString(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus().name(),
                t.getPriority().name(),
                t.getCreatedBy().getName(),
                t.getAssignedAgent() != null ? t.getAssignedAgent().getName() : null,
                t.getCategory() != null ? t.getCategory().getName() : null,
                t.getCreatedAt()
        );
    }
}
