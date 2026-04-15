package com.helpdesk.backend.modules.chat.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SendNoteRequest(
        @NotNull UUID sessionId,
        @NotBlank String content) {
}