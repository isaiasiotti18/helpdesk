package com.helpdesk.backend.modules.queue.application.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.backend.modules.queue.application.dto.CreateQueueRequest;
import com.helpdesk.backend.modules.queue.application.dto.QueueResponse;
import com.helpdesk.backend.modules.queue.domain.Queue;
import com.helpdesk.backend.modules.queue.domain.QueueAgent;
import com.helpdesk.backend.modules.queue.domain.QueueAgentRepository;
import com.helpdesk.backend.modules.queue.domain.QueueRepository;
import com.helpdesk.backend.shared.exception.BusinessException;
import com.helpdesk.backend.shared.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueRepository queueRepository;
    private final QueueAgentRepository queueAgentRepository;

    @Transactional
    public QueueResponse create(CreateQueueRequest request) {
        if (queueRepository.existsByName(request.name())) {
            throw new BusinessException("Queue name already exists", HttpStatus.CONFLICT);
        }

        Queue queue = Queue.builder()
                .name(request.name())
                .description(request.description())
                .maxAgents(request.maxAgents() != null ? request.maxAgents() : 10)
                .build();

        return QueueResponse.from(queueRepository.save(queue));
    }

    @Transactional(readOnly = true)
    public List<QueueResponse> listAll() {
        return queueRepository.findAll().stream()
                .map(QueueResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public QueueResponse getById(UUID queueId) {
        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new ResourceNotFoundException("Queue", queueId));
        return QueueResponse.from(queue);
    }

    @Transactional
    public void addAgent(UUID queueId, UUID agentId) {
        if (!queueRepository.existsById(queueId)) {
            throw new ResourceNotFoundException("Queue", queueId);
        }
        if (queueAgentRepository.existsByQueueIdAndAgentId(queueId, agentId)) {
            throw new BusinessException("Agent already in queue", HttpStatus.CONFLICT);
        }

        QueueAgent qa = QueueAgent.builder()
                .queueId(queueId)
                .agentId(agentId)
                .build();
        queueAgentRepository.save(qa);
    }

    @Transactional
    public void removeAgent(UUID queueId, UUID agentId) {
        queueAgentRepository.deleteByQueueIdAndAgentId(queueId, agentId);
    }
}
