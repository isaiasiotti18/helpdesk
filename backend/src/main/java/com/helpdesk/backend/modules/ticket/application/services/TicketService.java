package com.helpdesk.backend.modules.ticket.application.services;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.backend.modules.category.domain.Category;
import com.helpdesk.backend.modules.category.domain.CategoryRepository;
import com.helpdesk.backend.modules.ticket.domain.Priority;
import com.helpdesk.backend.modules.ticket.domain.Ticket;
import com.helpdesk.backend.modules.ticket.domain.TicketAssignment;
import com.helpdesk.backend.modules.ticket.domain.TicketStatus;
import com.helpdesk.backend.modules.ticket.domain.event.TicketAssignedEvent;
import com.helpdesk.backend.modules.ticket.domain.event.TicketClosedEvent;
import com.helpdesk.backend.modules.ticket.domain.event.TicketCreatedEvent;
import com.helpdesk.backend.modules.ticket.domain.repositories.TicketAssignmentRepository;
import com.helpdesk.backend.modules.ticket.domain.repositories.TicketRepository;
import com.helpdesk.backend.modules.ticket.domain.repositories.TicketSpecifications;
import com.helpdesk.backend.modules.ticket.application.dtos.CreateTicketRequest;
import com.helpdesk.backend.modules.ticket.application.dtos.TicketResponse;
import com.helpdesk.backend.modules.user.domain.User;
import com.helpdesk.backend.modules.user.domain.UserRepository;
import com.helpdesk.backend.shared.exception.BusinessException;
import com.helpdesk.backend.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TicketAssignmentRepository assignmentRepository;
    private final CategoryRepository categoryRepository;
    private final SlaService slaService;

    @Transactional
    public TicketResponse create(CreateTicketRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Priority priority = request.priority() != null ? request.priority() : Priority.MEDIUM;

        Ticket ticket = Ticket.create(request.title(), request.description(), priority, user);

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(UUID.fromString(request.categoryId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));
            ticket.setCategory(category);
        }

        ticket = ticketRepository.save(ticket);

        slaService.applySla(ticket);
        ticket = ticketRepository.save(ticket);

        eventPublisher.publishEvent(new TicketCreatedEvent(ticket.getId(), userId));

        return TicketResponse.from(ticket);
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> list(TicketStatus status, Priority priority, UUID agentId, UUID categoryId,
            Pageable pageable) {
        Specification<Ticket> spec = Specification
                .where(TicketSpecifications.fetchRelations())
                .and(TicketSpecifications.withStatus(status))
                .and(TicketSpecifications.withPriority(priority))
                .and(TicketSpecifications.withAssignedAgent(agentId))
                .and(TicketSpecifications.withCategory(categoryId));

        return ticketRepository.findAll(spec, pageable).map(TicketResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> myTickets(UUID userId, Pageable pageable) {
        return ticketRepository.findByCreatedById(userId, pageable)
                .map(TicketResponse::from);
    }

    @Transactional(readOnly = true)
    public TicketResponse getById(UUID ticketId, UUID userId, String userRole) {
        Ticket ticket = findOrThrow(ticketId);
        validateAccess(ticket, userId, userRole);
        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse close(UUID ticketId, UUID userId, String userRole) {
        Ticket ticket = findOrThrow(ticketId);
        validateAccess(ticket, userId, userRole);
        ticket.close();
        eventPublisher.publishEvent(new TicketClosedEvent(ticketId));
        ticket = ticketRepository.save(ticket);
        return TicketResponse.from(ticket);
    }

    private Ticket findOrThrow(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));
    }

    private void validateAccess(Ticket ticket, UUID userId, String userRole) {
        if ("CLIENT".equals(userRole)) {
            if (!ticket.getCreatedBy().getId().equals(userId)) {
                throw new BusinessException("Access denied", HttpStatus.FORBIDDEN);
            }
        }
        // AGENT e ADMIN podem acessar qualquer ticket
    }

    @Transactional
    public TicketResponse assign(UUID ticketId, UUID agentId) {
        try {
            Ticket ticket = findOrThrow(ticketId);
            User agent = userRepository.findById(agentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Agent", agentId));

            // Encerra assignment anterior se existir
            assignmentRepository.findByTicketIdAndEndedAtIsNull(ticketId)
                    .ifPresent(prev -> {
                        prev.end();
                        assignmentRepository.save(prev);
                    });

            ticket.assignTo(agent);
            ticket = ticketRepository.save(ticket);

            // Cria novo assignment
            assignmentRepository.save(TicketAssignment.builder()
                    .ticket(ticket)
                    .agent(agent)
                    .action("ASSIGNED")
                    .build());

            eventPublisher.publishEvent(new TicketAssignedEvent(ticketId, agentId));

            return TicketResponse.from(ticket);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException("Ticket already assigned by another agent", HttpStatus.CONFLICT);
        }
    }
}
