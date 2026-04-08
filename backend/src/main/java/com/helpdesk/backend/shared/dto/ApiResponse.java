package com.helpdesk.backend.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        T data,
        String error,
        Instant timestamp) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(data, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(null, message, Instant.now());
    }
}
