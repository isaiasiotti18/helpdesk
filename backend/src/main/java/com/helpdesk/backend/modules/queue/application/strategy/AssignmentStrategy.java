package com.helpdesk.backend.modules.queue.application.strategy;

import com.helpdesk.backend.modules.queue.domain.AgentStatus;

import java.util.List;
import java.util.Optional;

public interface AssignmentStrategy {
    Optional<AgentStatus> selectAgent(List<AgentStatus> availableAgents);
}
