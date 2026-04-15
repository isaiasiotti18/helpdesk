package com.helpdesk.backend.modules.ticket.application.dtos;

import com.helpdesk.backend.modules.ticket.domain.TicketActivity;

import java.time.LocalDateTime;

public record ActivityResponse(
        String id,
        String ticketId,
        String userId,
        String userName,
        String action,
        String detail,
        LocalDateTime createdAt) {
    public static ActivityResponse from(TicketActivity a) {
        return new ActivityResponse(
                a.getId().toString(),
                a.getTicket().getId().toString(),
                a.getUser() != null ? a.getUser().getId().toString() : null,
                a.getUser() != null ? a.getUser().getName() : "Sistema",
                a.getAction(),
                a.getDetail(),
                a.getCreatedAt());
    }
}
