package com.helpdesk.backend.modules.ticket.application.dtos;

import com.helpdesk.backend.modules.ticket.domain.Ticket;

import java.time.LocalDateTime;

public record TicketResponse(
        String id,
        String title,
        String description,
        String status,
        String priority,
        String createdById,
        String createdByName,
        String assignedAgentName,
        String categoryId,
        String categoryName,
        LocalDateTime createdAt,
        LocalDateTime closedAt,
        LocalDateTime slaFirstResponseDeadline,
        LocalDateTime slaResolutionDeadline,
        Boolean slaBbreached,
        LocalDateTime firstResponseAt) {
    public static TicketResponse from(Ticket t) {
        return new TicketResponse(
                t.getId().toString(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus().name(),
                t.getPriority().name(),
                t.getCreatedBy().getId().toString(),
                t.getCreatedBy().getName(),
                t.getAssignedAgent() != null ? t.getAssignedAgent().getName() : null,
                t.getCategory() != null ? t.getCategory().getId().toString() : null,
                t.getCategory() != null ? t.getCategory().getName() : null,
                t.getCreatedAt(),
                t.getClosedAt(),
                t.getSlaFirstResponseDeadline(),
                t.getSlaResolutionDeadline(),
                t.getSlaBbreached(),
                t.getFirstResponseAt());
    }
}
