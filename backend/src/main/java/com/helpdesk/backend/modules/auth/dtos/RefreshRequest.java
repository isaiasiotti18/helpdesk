package com.helpdesk.backend.modules.auth.dtos;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank String refreshToken) {
}
