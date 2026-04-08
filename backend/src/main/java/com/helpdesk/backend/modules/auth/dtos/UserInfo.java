package com.helpdesk.backend.modules.auth.dtos;

public record UserInfo(
        String id,
        String name,
        String email,
        String role) {
}