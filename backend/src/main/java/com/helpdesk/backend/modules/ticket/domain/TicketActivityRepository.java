package com.helpdesk.backend.modules.ticket.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketActivityRepository extends JpaRepository<TicketActivity, UUID> {

    @EntityGraph(attributePaths = {"user"})
    List<TicketActivity> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);
}
