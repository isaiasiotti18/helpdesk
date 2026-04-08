package com.helpdesk.backend.modules.ticket.domain.event;

import java.util.UUID;

public record TicketCreatedEvent(UUID ticketId, UUID createdBy) {
}
