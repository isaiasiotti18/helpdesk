package com.helpdesk.backend.shared.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async("chatThreadPool")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entityType, UUID entityId, String action, UUID userId, String payload) {
        auditLogRepository.save(AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .userId(userId)
                .payload(payload)
                .build());
    }
}