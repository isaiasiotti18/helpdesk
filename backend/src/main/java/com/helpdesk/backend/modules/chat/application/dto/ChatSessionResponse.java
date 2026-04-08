package com.helpdesk.backend.modules.chat.application.dto;

import com.helpdesk.backend.modules.chat.domain.ChatSession;

import java.time.LocalDateTime;

public record ChatSessionResponse(
        String id,
        String ticketId,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        boolean active) {
    public static ChatSessionResponse from(ChatSession cs) {
        return new ChatSessionResponse(
                cs.getId().toString(),
                cs.getTicket().getId().toString(),
                cs.getStartedAt(),
                cs.getEndedAt(),
                cs.isActive());
    }
}
