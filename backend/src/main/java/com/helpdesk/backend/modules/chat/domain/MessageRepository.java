package com.helpdesk.backend.modules.chat.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    // Cursor-based: mensagens antes de um timestamp
    List<Message> findTop50BySessionIdAndSentAtBeforeOrderBySentAtDesc(UUID sessionId, LocalDateTime before);

    // Mensagens mais recentes (primeira carga)
    List<Message> findTop50BySessionIdOrderBySentAtDesc(UUID sessionId);

    // Mensagens visíveis pro cliente (exclui internas)
    Page<Message> findBySessionIdAndIsInternalFalseOrderBySentAtAsc(UUID sessionId, Pageable pageable);

    // Todas as mensagens (agente vê tudo)
    Page<Message> findBySessionIdOrderBySentAtAsc(UUID sessionId, Pageable pageable);

    // Cursor-based sem internas
    List<Message> findTop50BySessionIdAndIsInternalFalseAndSentAtBeforeOrderBySentAtDesc(UUID sessionId,
            LocalDateTime before);

    List<Message> findTop50BySessionIdAndIsInternalFalseOrderBySentAtDesc(UUID sessionId);
}
