package com.helpdesk.backend.modules.chat.application.services;

import com.helpdesk.backend.modules.chat.application.dto.MessageResponse;
import com.helpdesk.backend.modules.chat.domain.*;
import com.helpdesk.backend.modules.ticket.application.services.SlaService;
import com.helpdesk.backend.modules.user.domain.Role;
import com.helpdesk.backend.modules.user.domain.User;
import com.helpdesk.backend.modules.user.domain.UserRepository;
import com.helpdesk.backend.shared.exception.BusinessException;
import com.helpdesk.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final MessageRepository messageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;
    private final AsyncMessagePersistence asyncPersistence;
    private final SlaService slaService;

    /**
     * Persiste mensagem de forma assíncrona e retorna o DTO imediatamente.
     * Usado pelo WebSocket handler para não bloquear a thread do STOMP.
     */
    public MessageResponse sendAsync(UUID sessionId, UUID senderId, String content) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession", sessionId));

        if (!session.isActive()) {
            throw new BusinessException("Chat session is closed");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", senderId));

        Message message = Message.builder()
                .session(session)
                .sender(sender)
                .content(content)
                .messageType(MessageType.TEXT)
                .build();

        // Persiste async — não bloqueia o WebSocket
        asyncPersistence.persist(message);

        if (sender.getRole() == Role.AGENT) {
            slaService.recordFirstResponse(session.getTicket().getId());
        }

        return MessageResponse.from(message);
    }

    /**
     * Persiste mensagem de forma síncrona.
     * Usado pelo endpoint REST (fallback HTTP).
     */
    @Transactional
    public MessageResponse sendSync(UUID sessionId, UUID senderId, String content) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession", sessionId));

        if (!session.isActive()) {
            throw new BusinessException("Chat session is closed");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", senderId));

        Message message = Message.builder()
                .session(session)
                .sender(sender)
                .content(content)
                .messageType(MessageType.TEXT)
                .build();

        message = messageRepository.save(message);

        if (sender.getRole() == Role.AGENT) {
            slaService.recordFirstResponse(session.getTicket().getId());
        }

        return MessageResponse.from(message);
    }

    // Alterar getMessages para receber flag
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(UUID sessionId, boolean includeInternal, Pageable pageable) {
        Page<Message> messages;
        if (includeInternal) {
            messages = messageRepository.findBySessionIdOrderBySentAtAsc(sessionId, pageable);
        } else {
            messages = messageRepository.findBySessionIdAndIsInternalFalseOrderBySentAtAsc(sessionId, pageable);
        }
        return messages.map(MessageResponse::from);
    }

    // Alterar getMessagesCursor para receber flag
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesCursor(UUID sessionId, LocalDateTime before, boolean includeInternal) {
        List<Message> messages;
        if (includeInternal) {
            if (before != null) {
                messages = messageRepository.findTop50BySessionIdAndSentAtBeforeOrderBySentAtDesc(sessionId, before);
            } else {
                messages = messageRepository.findTop50BySessionIdOrderBySentAtDesc(sessionId);
            }
        } else {
            if (before != null) {
                messages = messageRepository
                        .findTop50BySessionIdAndIsInternalFalseAndSentAtBeforeOrderBySentAtDesc(sessionId, before);
            } else {
                messages = messageRepository.findTop50BySessionIdAndIsInternalFalseOrderBySentAtDesc(sessionId);
            }
        }
        return messages.reversed().stream()
                .map(MessageResponse::from)
                .toList();
    }

    // Novo método para notas internas
    @Transactional
    public MessageResponse sendNote(UUID sessionId, UUID senderId, String content) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession", sessionId));

        if (!session.isActive()) {
            throw new BusinessException("Chat session is closed");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", senderId));

        Message message = Message.builder()
                .session(session)
                .sender(sender)
                .content(content)
                .messageType(MessageType.NOTE)
                .isInternal(true)
                .build();

        message = messageRepository.save(message);
        return MessageResponse.from(message);
    }
}
