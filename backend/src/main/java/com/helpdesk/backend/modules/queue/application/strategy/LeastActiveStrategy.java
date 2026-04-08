package com.helpdesk.backend.modules.queue.application.strategy;

import com.helpdesk.backend.modules.queue.domain.AgentStatus;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class LeastActiveStrategy implements AssignmentStrategy {

    @Override
    public Optional<AgentStatus> selectAgent(List<AgentStatus> availableAgents) {
        return availableAgents.stream()
                .filter(AgentStatus::isAvailable)
                .min(Comparator.comparingInt(AgentStatus::getActiveTickets));
    }
}
