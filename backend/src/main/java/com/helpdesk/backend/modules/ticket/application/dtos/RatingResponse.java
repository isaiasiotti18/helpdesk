package com.helpdesk.backend.modules.ticket.application.dtos;

import com.helpdesk.backend.modules.ticket.domain.TicketRating;

import java.time.LocalDateTime;

public record RatingResponse(
        String id,
        String ticketId,
        String userName,
        Integer score,
        String comment,
        LocalDateTime createdAt
) {
    public static RatingResponse from(TicketRating r) {
        return new RatingResponse(
                r.getId().toString(),
                r.getTicket().getId().toString(),
                r.getUser().getName(),
                r.getScore(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}
