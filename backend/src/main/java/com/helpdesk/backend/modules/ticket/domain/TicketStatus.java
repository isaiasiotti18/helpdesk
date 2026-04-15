package com.helpdesk.backend.modules.ticket.domain;

import java.util.Set;
import java.util.Map;

public enum TicketStatus {
    OPEN,
    IN_QUEUE,
    IN_PROGRESS,
    TRANSFERRED,
    RESOLVED,
    CLOSED;

    private static final Map<TicketStatus, Set<TicketStatus>> TRANSITIONS = Map.of(
            OPEN, Set.of(IN_QUEUE, IN_PROGRESS, CLOSED),
            IN_QUEUE, Set.of(IN_PROGRESS, CLOSED),
            IN_PROGRESS, Set.of(TRANSFERRED, RESOLVED, CLOSED, IN_QUEUE),
            TRANSFERRED, Set.of(IN_PROGRESS, CLOSED),
            RESOLVED, Set.of(CLOSED, IN_PROGRESS),
            CLOSED, Set.of());

    public boolean canTransitionTo(TicketStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
