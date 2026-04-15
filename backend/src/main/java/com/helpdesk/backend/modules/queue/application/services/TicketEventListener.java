package com.helpdesk.backend.modules.queue.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.helpdesk.backend.modules.ticket.application.services.TicketActivityService;
import com.helpdesk.backend.modules.ticket.domain.event.TicketAssignedEvent;
import com.helpdesk.backend.modules.ticket.domain.event.TicketClosedEvent;
import com.helpdesk.backend.modules.ticket.domain.event.TicketCreatedEvent;
import com.helpdesk.backend.modules.ticket.domain.repositories.TicketRepository;
import com.helpdesk.backend.shared.audit.AuditService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketEventListener {

    private final AssignmentService assignmentService;
    private final AgentStatusService agentStatusService;
    private final TicketRepository ticketRepository;
    private final AuditService auditService;
    private final TicketActivityService activityService;

    @EventListener
    public void onTicketCreated(TicketCreatedEvent event) {
        log.info("Ticket created: {}, attempting auto-assign", event.ticketId());
        activityService.record(event.ticketId(), event.createdBy(), "CREATED", null);
        auditService.log("TICKET", event.ticketId(), "CREATED", event.createdBy(), null);
        assignmentService.autoAssign(event.ticketId());
    }

    @EventListener
    public void onTicketAssigned(TicketAssignedEvent event) {
        activityService.record(event.ticketId(), event.agentId(), "ASSIGNED", null);
    }

    @EventListener
    public void onTicketClosed(TicketClosedEvent event) {
        activityService.record(event.ticketId(), null, "CLOSED", null);
        auditService.log("TICKET", event.ticketId(), "CLOSED", null, null);
        ticketRepository.findById(event.ticketId()).ifPresent(ticket -> {
            if (ticket.getAssignedAgent() != null) {
                agentStatusService.decrementTickets(ticket.getAssignedAgent().getId());
            }
        });
    }
}
