package com.helpdesk.backend.modules.ticket.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class TicketStatusTest {

    @ParameterizedTest
    @DisplayName("Transições válidas")
    @CsvSource({
            "OPEN, IN_QUEUE",
            "OPEN, IN_PROGRESS",
            "OPEN, CLOSED",
            "IN_QUEUE, IN_PROGRESS",
            "IN_QUEUE, CLOSED",
            "IN_PROGRESS, TRANSFERRED",
            "IN_PROGRESS, RESOLVED",
            "IN_PROGRESS, CLOSED",
            "TRANSFERRED, IN_PROGRESS",
            "TRANSFERRED, CLOSED",
            "RESOLVED, CLOSED",
            "RESOLVED, IN_PROGRESS"
    })
    void shouldAllowValidTransitions(TicketStatus from, TicketStatus to) {
        assertThat(from.canTransitionTo(to)).isTrue();
    }

    @ParameterizedTest
    @DisplayName("Transições inválidas")
    @CsvSource({
            "OPEN, RESOLVED",
            "OPEN, TRANSFERRED",
            "CLOSED, OPEN",
            "CLOSED, IN_PROGRESS",
            "CLOSED, RESOLVED",
            "RESOLVED, OPEN",
            "RESOLVED, TRANSFERRED",
            "IN_QUEUE, OPEN"
    })
    void shouldRejectInvalidTransitions(TicketStatus from, TicketStatus to) {
        assertThat(from.canTransitionTo(to)).isFalse();
    }

    @Test
    @DisplayName("CLOSED não deve transicionar pra nenhum status")
    void closedShouldNotTransitionToAnything() {
        for (TicketStatus status : TicketStatus.values()) {
            assertThat(TicketStatus.CLOSED.canTransitionTo(status)).isFalse();
        }
    }
}
