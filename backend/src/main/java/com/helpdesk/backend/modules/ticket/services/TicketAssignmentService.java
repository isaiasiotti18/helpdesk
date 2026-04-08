package com.helpdesk.backend.modules.ticket.services;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.backend.modules.ticket.domain.Ticket;
import com.helpdesk.backend.modules.ticket.domain.event.TicketAssignedEvent;
import com.helpdesk.backend.modules.ticket.domain.repositories.TicketRepository;
import com.helpdesk.backend.modules.ticket.dtos.TicketResponse;
import com.helpdesk.backend.modules.user.domain.User;
import com.helpdesk.backend.modules.user.domain.UserRepository;
import com.helpdesk.backend.shared.exception.BusinessException;
import com.helpdesk.backend.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketAssignmentService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TicketResponse assign(UUID ticketId, UUID agentId) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));
            User agent = userRepository.findById(agentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Agent", agentId));

            ticket.assignTo(agent);
            ticket = ticketRepository.save(ticket);

            eventPublisher.publishEvent(new TicketAssignedEvent(ticketId, agentId));

            return TicketResponse.from(ticket);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException("Ticket already assigned by another agent", HttpStatus.CONFLICT);
        }
    }
}
