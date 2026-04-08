package com.helpdesk.backend.modules.ticket.dtos;

import com.helpdesk.backend.modules.ticket.domain.Ticket;

import java.time.LocalDateTime;

public record TicketResponse(
        String id,
        String title,
        String description,
        String status,
        String priority,
        String createdByName,
        String assignedAgentName,
        LocalDateTime createdAt,
        LocalDateTime closedAt) {

    public static TicketResponse from(Ticket t) {
        return new TicketResponse(
                t.getId().toString(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus().name(),
                t.getPriority().name(),
                t.getCreatedBy().getName(),
                t.getAssignedAgent() != null ? t.getAssignedAgent().getName() : null,
                t.getCreatedAt(),
                t.getClosedAt());
    }
}
