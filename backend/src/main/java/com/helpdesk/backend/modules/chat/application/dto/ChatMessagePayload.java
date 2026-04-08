package com.helpdesk.backend.modules.chat.application.dto;

public record ChatMessagePayload(
        String sessionId,
        String content) {
}
