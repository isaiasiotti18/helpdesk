package com.helpdesk.backend.modules.ticket.dtos;

import com.helpdesk.backend.modules.ticket.domain.TicketStatus;

public record UpdateStatusRequest(
        TicketStatus status) {
}
