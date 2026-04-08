package com.helpdesk.backend.modules.ticket.domain.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.helpdesk.backend.modules.ticket.domain.Priority;
import com.helpdesk.backend.modules.ticket.domain.Ticket;
import com.helpdesk.backend.modules.ticket.domain.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {

    @EntityGraph(attributePaths = { "createdBy", "assignedAgent" })
    Page<Ticket> findByCreatedById(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = { "createdBy", "assignedAgent" })
    Page<Ticket> findByAssignedAgentId(UUID agentId, Pageable pageable);

    @EntityGraph(attributePaths = { "createdBy", "assignedAgent" })
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    @EntityGraph(attributePaths = { "createdBy", "assignedAgent" })
    Page<Ticket> findByPriority(Priority priority, Pageable pageable);

    @EntityGraph(attributePaths = { "createdBy", "assignedAgent" })
    Page<Ticket> findByStatusAndPriority(TicketStatus status, Priority priority, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = { "createdBy", "assignedAgent" })
    Page<Ticket> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = { "createdBy", "assignedAgent" })
    Optional<Ticket> findById(UUID id);
}