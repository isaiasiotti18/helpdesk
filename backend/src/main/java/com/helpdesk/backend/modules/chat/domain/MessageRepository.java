package com.helpdesk.backend.modules.chat.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    Page<Message> findBySessionIdOrderBySentAtAsc(UUID sessionId, Pageable pageable);

    // Cursor-based: mensagens antes de um timestamp
    List<Message> findTop50BySessionIdAndSentAtBeforeOrderBySentAtDesc(UUID sessionId, LocalDateTime before);

    // Mensagens mais recentes (primeira carga)
    List<Message> findTop50BySessionIdOrderBySentAtDesc(UUID sessionId);
}
