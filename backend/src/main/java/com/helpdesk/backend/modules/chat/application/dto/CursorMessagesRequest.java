package com.helpdesk.backend.modules.chat.application.dto;

import java.time.LocalDateTime;

public record CursorMessagesRequest(
        LocalDateTime before,
        int limit) {
    public CursorMessagesRequest {
        if (limit <= 0 || limit > 100)
            limit = 50;
    }
}