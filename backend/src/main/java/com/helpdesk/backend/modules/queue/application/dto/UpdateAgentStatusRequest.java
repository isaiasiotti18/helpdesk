package com.helpdesk.backend.modules.queue.application.dto;

import com.helpdesk.backend.modules.queue.domain.AgentOnlineStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAgentStatusRequest(
                @NotNull AgentOnlineStatus status) {
}
