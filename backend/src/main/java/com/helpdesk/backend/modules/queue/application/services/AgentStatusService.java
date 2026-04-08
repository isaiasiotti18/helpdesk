package com.helpdesk.backend.modules.queue.application.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.backend.modules.queue.application.dto.AgentStatusResponse;
import com.helpdesk.backend.modules.queue.domain.AgentOnlineStatus;
import com.helpdesk.backend.modules.queue.domain.AgentStatus;
import com.helpdesk.backend.modules.queue.domain.AgentStatusRepository;
import com.helpdesk.backend.modules.user.domain.User;
import com.helpdesk.backend.modules.user.domain.UserRepository;
import com.helpdesk.backend.shared.exception.ResourceNotFoundException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentStatusService {

    private final AgentStatusRepository agentStatusRepository;
    private final UserRepository userRepository;

    @Transactional
    public AgentStatusResponse updateStatus(UUID agentId, AgentOnlineStatus newStatus) {
        AgentStatus status = getOrCreate(agentId);

        switch (newStatus) {
            case ONLINE -> status.goOnline();
            case OFFLINE -> status.goOffline();
            default -> status.setStatus(newStatus);
        }

        return AgentStatusResponse.from(agentStatusRepository.save(status));
    }

    @Transactional(readOnly = true)
    public AgentStatusResponse getStatus(UUID agentId) {
        AgentStatus status = agentStatusRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("AgentStatus", agentId));
        return AgentStatusResponse.from(status);
    }

    @Transactional
    public void incrementTickets(UUID agentId) {
        AgentStatus status = getOrCreate(agentId);
        status.incrementActiveTickets();
        agentStatusRepository.save(status);
    }

    @Transactional
    public void decrementTickets(UUID agentId) {
        agentStatusRepository.findById(agentId).ifPresent(status -> {
            status.decrementActiveTickets();
            agentStatusRepository.save(status);
        });
    }

    private AgentStatus getOrCreate(UUID agentId) {
        return agentStatusRepository.findById(agentId)
                .orElseGet(() -> {
                    User agent = userRepository.findById(agentId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", agentId));
                    AgentStatus newStatus = AgentStatus.builder()
                            .agent(agent)
                            .build();
                    return agentStatusRepository.save(newStatus);
                });
    }
}
