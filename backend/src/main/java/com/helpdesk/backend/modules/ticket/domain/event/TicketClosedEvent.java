package com.helpdesk.backend.modules.ticket.domain.event;

import java.util.UUID;

public record TicketClosedEvent(UUID ticketId) {
}
