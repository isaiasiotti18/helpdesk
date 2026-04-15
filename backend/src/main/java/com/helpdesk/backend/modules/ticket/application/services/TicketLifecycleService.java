package com.helpdesk.backend.modules.ticket.application.services;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.backend.modules.ticket.domain.Ticket;
import com.helpdesk.backend.modules.ticket.domain.TicketStatus;
import com.helpdesk.backend.modules.ticket.domain.event.TicketClosedEvent;
import com.helpdesk.backend.modules.ticket.domain.repositories.TicketRepository;
import com.helpdesk.backend.modules.ticket.application.dtos.TicketResponse;
import com.helpdesk.backend.shared.exception.BusinessException;
import com.helpdesk.backend.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketLifecycleService {

    private final TicketRepository ticketRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TicketResponse updateStatus(UUID ticketId, TicketStatus newStatus) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

            ticket.transitionTo(newStatus);

            if (newStatus == TicketStatus.CLOSED) {
                eventPublisher.publishEvent(new TicketClosedEvent(ticketId));
            }

            ticket = ticketRepository.save(ticket);
            return TicketResponse.from(ticket);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException("Ticket was modified concurrently, please retry", HttpStatus.CONFLICT);
        }
    }

    @Transactional
    public TicketResponse close(UUID ticketId) {
        return updateStatus(ticketId, TicketStatus.CLOSED);
    }
}
