package com.helpdesk.backend.modules.ticket.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface TicketRatingRepository extends JpaRepository<TicketRating, UUID> {
    Optional<TicketRating> findByTicketId(UUID ticketId);
    boolean existsByTicketId(UUID ticketId);

    @Query("SELECT AVG(r.score) FROM TicketRating r")
    Double findAverageScore();

    @Query("SELECT AVG(r.score) FROM TicketRating r WHERE r.ticket.assignedAgent.id = :agentId")
    Double findAverageScoreByAgent(UUID agentId);
}
