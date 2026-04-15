package com.helpdesk.backend.modules.chat.application.dto;

import com.helpdesk.backend.modules.chat.domain.Message;

import java.time.LocalDateTime;

public record MessageResponse(
        String id,
        String sessionId,
        String senderId,
        String senderName,
        String content,
        String messageType,
        LocalDateTime sentAt,
        Boolean isInternal) {
    public static MessageResponse from(Message m) {
        return new MessageResponse(
                m.getId().toString(),
                m.getSession().getId().toString(),
                m.getSender().getId().toString(),
                m.getSender().getName(),
                m.getContent(),
                m.getMessageType().name(),
                m.getSentAt(),
                m.getIsInternal());
    }
}
