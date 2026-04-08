package com.helpdesk.backend.modules.chat.application.services;

import com.helpdesk.backend.modules.chat.application.dto.ChatSessionResponse;
import com.helpdesk.backend.modules.chat.domain.ChatSession;
import com.helpdesk.backend.modules.chat.domain.ChatSessionRepository;
import com.helpdesk.backend.modules.ticket.domain.Ticket;
import com.helpdesk.backend.modules.ticket.domain.TicketStatus;
import com.helpdesk.backend.modules.ticket.domain.repositories.TicketRepository;
import com.helpdesk.backend.shared.exception.BusinessException;
import com.helpdesk.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public ChatSessionResponse createForTicket(UUID ticketId) {
        if (chatSessionRepository.existsByTicketId(ticketId)) {
            throw new BusinessException("Chat session already exists for this ticket", HttpStatus.CONFLICT);
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

        if (ticket.getStatus() != TicketStatus.IN_PROGRESS) {
            throw new BusinessException("Ticket must be IN_PROGRESS to start chat");
        }

        ChatSession session = ChatSession.builder()
                .ticket(ticket)
                .build();

        return ChatSessionResponse.from(chatSessionRepository.save(session));
    }

    @Transactional(readOnly = true)
    public ChatSessionResponse getByTicketId(UUID ticketId) {
        ChatSession session = chatSessionRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession for ticket", ticketId));
        return ChatSessionResponse.from(session);
    }

    @Transactional(readOnly = true)
    public ChatSessionResponse getById(UUID sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession", sessionId));
        return ChatSessionResponse.from(session);
    }

    @Transactional
    public void endSession(UUID sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession", sessionId));
        session.end();
        chatSessionRepository.save(session);
    }
}
