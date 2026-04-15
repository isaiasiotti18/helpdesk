package com.helpdesk.backend.modules.chat.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCannedRequest(
        @NotBlank String title,
        @NotBlank String content,
        String shortcut,
        String category,
        Boolean isShared
) {}
