package com.helpdesk.backend.shared.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "user_id")
    private UUID userId;

    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}