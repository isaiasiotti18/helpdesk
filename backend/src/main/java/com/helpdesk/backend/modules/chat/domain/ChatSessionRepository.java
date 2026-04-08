package com.helpdesk.backend.modules.chat.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    Optional<ChatSession> findByTicketId(UUID ticketId);

    boolean existsByTicketId(UUID ticketId);
}
