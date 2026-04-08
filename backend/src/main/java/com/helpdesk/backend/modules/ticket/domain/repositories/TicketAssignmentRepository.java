package com.helpdesk.backend.modules.ticket.domain.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.helpdesk.backend.modules.ticket.domain.TicketAssignment;

public interface TicketAssignmentRepository extends JpaRepository<TicketAssignment, UUID> {
    List<TicketAssignment> findByTicketIdOrderByAssignedAtDesc(UUID ticketId);

    Optional<TicketAssignment> findByTicketIdAndEndedAtIsNull(UUID ticketId);
}
