package com.helpdesk.backend.modules.auth.dtos;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserInfo user) {
}