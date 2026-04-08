package com.helpdesk.backend.modules.queue.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.backend.modules.queue.application.strategy.AssignmentStrategy;
import com.helpdesk.backend.modules.queue.domain.AgentOnlineStatus;
import com.helpdesk.backend.modules.queue.domain.AgentStatus;
import com.helpdesk.backend.modules.queue.domain.AgentStatusRepository;
import com.helpdesk.backend.modules.ticket.domain.Ticket;
import com.helpdesk.backend.modules.ticket.domain.repositories.TicketRepository;
import com.helpdesk.backend.modules.user.domain.User;
import com.helpdesk.backend.modules.user.domain.UserRepository;
import com.helpdesk.backend.shared.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AgentStatusRepository agentStatusRepository;
    private final AgentStatusService agentStatusService;
    private final AssignmentStrategy assignmentStrategy;

    @Transactional
    public Optional<UUID> autoAssign(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

        List<AgentStatus> available = ticket.getQueueId() != null
                ? agentStatusRepository.findAvailableAgentsByQueue(ticket.getQueueId(), AgentOnlineStatus.ONLINE)
                : agentStatusRepository.findAllAvailableAgents(AgentOnlineStatus.ONLINE);

        Optional<AgentStatus> selected = assignmentStrategy.selectAgent(available);

        if (selected.isEmpty()) {
            log.info("No available agent for ticket {}", ticketId);
            return Optional.empty();
        }

        AgentStatus agent = selected.get();

        try {
            User agentUser = userRepository.findById(agent.getAgentId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", agent.getAgentId()));

            ticket.assignTo(agentUser);
            ticketRepository.save(ticket);

            agentStatusService.incrementTickets(agent.getAgentId());

            log.info("Ticket {} assigned to agent {}", ticketId, agent.getAgentId());
            return Optional.of(agent.getAgentId());

        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Concurrent assignment conflict for ticket {}, retrying not implemented", ticketId);
            return Optional.empty();
        }
    }
}
