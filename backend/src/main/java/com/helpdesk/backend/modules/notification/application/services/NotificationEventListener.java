package com.helpdesk.backend.modules.notification.application.services;

import com.helpdesk.backend.modules.ticket.domain.event.TicketAssignedEvent;
import com.helpdesk.backend.modules.ticket.domain.event.TicketClosedEvent;
import com.helpdesk.backend.modules.ticket.domain.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final TicketRepository ticketRepository;

    @EventListener
    public void onTicketAssigned(TicketAssignedEvent event) {
        notificationService.send(
                event.agentId(),
                "TICKET_ASSIGNED",
                "Novo ticket atribuído",
                "Você recebeu um novo ticket para atendimento.",
                event.ticketId()
        );

        ticketRepository.findById(event.ticketId()).ifPresent(ticket -> {
            notificationService.send(
                    ticket.getCreatedBy().getId(),
                    "TICKET_ASSIGNED",
                    "Ticket em atendimento",
                    "Seu ticket foi atribuído a um agente.",
                    event.ticketId()
            );
        });
    }

    @EventListener
    public void onTicketClosed(TicketClosedEvent event) {
        ticketRepository.findById(event.ticketId()).ifPresent(ticket -> {
            notificationService.send(
                    ticket.getCreatedBy().getId(),
                    "TICKET_CLOSED",
                    "Ticket fechado",
                    "Seu ticket foi encerrado.",
                    event.ticketId()
            );
        });
    }
}
