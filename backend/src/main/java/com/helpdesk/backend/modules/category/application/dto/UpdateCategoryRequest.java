package com.helpdesk.backend.modules.category.application.dto;

import java.util.UUID;

public record UpdateCategoryRequest(
        String name,
        String description,
        UUID queueId,
        Boolean isActive
) {}
