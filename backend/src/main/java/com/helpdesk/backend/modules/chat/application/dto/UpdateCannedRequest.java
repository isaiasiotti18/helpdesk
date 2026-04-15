package com.helpdesk.backend.modules.chat.application.dto;

public record UpdateCannedRequest(
        String title,
        String content,
        String shortcut,
        String category,
        Boolean isShared
) {}
