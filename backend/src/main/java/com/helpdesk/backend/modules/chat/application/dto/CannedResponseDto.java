package com.helpdesk.backend.modules.chat.application.dto;

import com.helpdesk.backend.modules.chat.domain.CannedResponse;

import java.time.LocalDateTime;

public record CannedResponseDto(
        String id,
        String title,
        String content,
        String shortcut,
        String category,
        String createdByName,
        Boolean isShared,
        LocalDateTime createdAt
) {
    public static CannedResponseDto from(CannedResponse c) {
        return new CannedResponseDto(
                c.getId().toString(),
                c.getTitle(),
                c.getContent(),
                c.getShortcut(),
                c.getCategory(),
                c.getCreatedBy().getName(),
                c.getIsShared(),
                c.getCreatedAt()
        );
    }
}
