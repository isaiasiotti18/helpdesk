package com.helpdesk.backend.modules.category.application.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateCategoryRequest(
        @NotBlank String name,
        String description,
        UUID queueId
) {}
