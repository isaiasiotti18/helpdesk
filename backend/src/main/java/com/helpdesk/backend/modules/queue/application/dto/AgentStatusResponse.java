package com.helpdesk.backend.modules.queue.application.dto;

import java.time.LocalDateTime;

import com.helpdesk.backend.modules.queue.domain.AgentStatus;

public record AgentStatusResponse(
        String agentId,
        String agentName,
        String status,
        Integer activeTickets,
        Integer maxTickets,
        LocalDateTime lastSeen) {
    public static AgentStatusResponse from(AgentStatus as) {
        return new AgentStatusResponse(
                as.getAgentId().toString(),
                as.getAgent().getName(),
                as.getStatus().name(),
                as.getActiveTickets(),
                as.getMaxTickets(),
                as.getLastSeen());
    }
}
