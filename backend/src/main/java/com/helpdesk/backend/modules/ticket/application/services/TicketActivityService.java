package com.helpdesk.backend.modules.ticket.application.services;

import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.backend.modules.ticket.application.dtos.ActivityResponse;
import com.helpdesk.backend.modules.ticket.domain.Ticket;
import com.helpdesk.backend.modules.ticket.domain.TicketActivity;
import com.helpdesk.backend.modules.ticket.domain.TicketActivityRepository;
import com.helpdesk.backend.modules.ticket.domain.repositories.TicketRepository;
import com.helpdesk.backend.modules.user.domain.User;
import com.helpdesk.backend.modules.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketActivityService {

    private final TicketActivityRepository activityRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ActivityResponse> getByTicketId(UUID ticketId) {
        return activityRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(ActivityResponse::from)
                .toList();
    }

    @Async("chatThreadPool")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID ticketId, UUID userId, String action, String detail) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket == null)
            return;

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        activityRepository.save(TicketActivity.builder()
                .ticket(ticket)
                .user(user)
                .action(action)
                .detail(detail)
                .build());
    }
}
