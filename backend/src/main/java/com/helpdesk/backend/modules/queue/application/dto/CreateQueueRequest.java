package com.helpdesk.backend.modules.queue.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateQueueRequest(
                @NotBlank String name,
                String description,
                Integer maxAgents) {
}
