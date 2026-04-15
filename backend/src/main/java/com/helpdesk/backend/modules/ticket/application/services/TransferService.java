package com.helpdesk.backend.modules.ticket.application.services;

import com.helpdesk.backend.modules.queue.application.services.AgentStatusService;
import com.helpdesk.backend.modules.queue.application.services.AssignmentService;
import com.helpdesk.backend.modules.ticket.application.dtos.TransferRequest;
import com.helpdesk.backend.modules.ticket.application.dtos.TicketResponse;
import com.helpdesk.backend.modules.ticket.domain.Ticket;
import com.helpdesk.backend.modules.ticket.domain.TicketAssignment;
import com.helpdesk.backend.modules.ticket.domain.TicketStatus;
import com.helpdesk.backend.modules.ticket.domain.repositories.TicketAssignmentRepository;
import com.helpdesk.backend.modules.ticket.domain.repositories.TicketRepository;
import com.helpdesk.backend.modules.user.domain.User;
import com.helpdesk.backend.modules.user.domain.UserRepository;
import com.helpdesk.backend.shared.exception.BusinessException;
import com.helpdesk.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketAssignmentRepository assignmentRepository;
    private final AgentStatusService agentStatusService;
    private final AssignmentService assignmentService;

    @Transactional
    public TicketResponse transferToAgent(UUID ticketId, UUID newAgentId, UUID currentUserId, String reason) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

        if (ticket.getStatus() != TicketStatus.IN_PROGRESS) {
            throw new BusinessException("Ticket must be IN_PROGRESS to transfer", HttpStatus.BAD_REQUEST);
        }

        User newAgent = userRepository.findById(newAgentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent", newAgentId));

        // Encerrar assignment atual
        UUID previousAgentId = null;
        if (ticket.getAssignedAgent() != null) {
            previousAgentId = ticket.getAssignedAgent().getId();
            assignmentRepository.findByTicketIdAndEndedAtIsNull(ticketId)
                    .ifPresent(prev -> {
                        prev.end();
                        assignmentRepository.save(prev);
                    });
            agentStatusService.decrementTickets(previousAgentId);
        }

        // Transferir
        ticket.transfer(newAgent);
        ticket = ticketRepository.save(ticket);

        // Criar novo assignment
        assignmentRepository.save(TicketAssignment.builder()
                .ticket(ticket)
                .agent(newAgent)
                .action("TRANSFERRED")
                .build());

        agentStatusService.incrementTickets(newAgentId);

        log.info("Ticket {} transferred from {} to {} — reason: {}",
                ticketId, previousAgentId, newAgentId, reason);

        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse transferToQueue(UUID ticketId, UUID queueId, UUID currentUserId, String reason) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

        if (ticket.getStatus() != TicketStatus.IN_PROGRESS) {
            throw new BusinessException("Ticket must be IN_PROGRESS to transfer", HttpStatus.BAD_REQUEST);
        }

        // Encerrar assignment atual
        if (ticket.getAssignedAgent() != null) {
            UUID previousAgentId = ticket.getAssignedAgent().getId();
            assignmentRepository.findByTicketIdAndEndedAtIsNull(ticketId)
                    .ifPresent(prev -> {
                        prev.end();
                        assignmentRepository.save(prev);
                    });
            agentStatusService.decrementTickets(previousAgentId);
        }

        // Mover pra nova fila
        ticket.setQueueId(queueId);
        ticket.setAssignedAgent(null);
        ticket.transitionTo(TicketStatus.IN_QUEUE);
        ticket = ticketRepository.save(ticket);

        log.info("Ticket {} transferred to queue {} — reason: {}", ticketId, queueId, reason);

        // Tentar auto-assign na nova fila
        assignmentService.autoAssign(ticketId);

        return TicketResponse.from(ticket);
    }
}
