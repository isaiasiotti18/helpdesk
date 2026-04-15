package com.helpdesk.backend.modules.ticket.application.services;

import com.helpdesk.backend.modules.ticket.domain.SlaPolicyRepository;
import com.helpdesk.backend.modules.ticket.domain.Ticket;
import com.helpdesk.backend.modules.ticket.domain.TicketStatus;
import com.helpdesk.backend.modules.ticket.domain.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlaService {

    private final TicketRepository ticketRepository;
    private final SlaPolicyRepository slaPolicyRepository;

    @Transactional
    public void applySla(Ticket ticket) {
        slaPolicyRepository.findByPriorityAndIsActiveTrue(ticket.getPriority())
                .ifPresent(policy -> {
                    LocalDateTime now = LocalDateTime.now();
                    ticket.setSlaPolicy(policy);
                    ticket.setSlaFirstResponseDeadline(now.plusMinutes(policy.getFirstResponseMinutes()));
                    ticket.setSlaResolutionDeadline(now.plusMinutes(policy.getResolutionMinutes()));
                    ticket.setSlaBbreached(false);
                });
    }

    @Transactional
    public void recordFirstResponse(UUID ticketId) {
        ticketRepository.findById(ticketId).ifPresent(ticket -> {
            if (ticket.getFirstResponseAt() == null) {
                ticket.setFirstResponseAt(LocalDateTime.now());
                ticketRepository.save(ticket);
            }
        });
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void checkBreaches() {
        LocalDateTime now = LocalDateTime.now();

        List<Ticket> atRisk = ticketRepository.findAll().stream()
                .filter(t -> !t.getSlaBbreached())
                .filter(t -> t.getSlaResolutionDeadline() != null)
                .filter(t -> t.getStatus() != TicketStatus.CLOSED && t.getStatus() != TicketStatus.RESOLVED)
                .filter(t -> now.isAfter(t.getSlaResolutionDeadline()) ||
                        (t.getFirstResponseAt() == null && t.getSlaFirstResponseDeadline() != null && now.isAfter(t.getSlaFirstResponseDeadline())))
                .toList();

        for (Ticket ticket : atRisk) {
            ticket.setSlaBbreached(true);
            ticketRepository.save(ticket);
            log.warn("SLA breached for ticket {}", ticket.getId());
        }

        if (!atRisk.isEmpty()) {
            log.info("SLA check: {} tickets breached", atRisk.size());
        }
    }
}
