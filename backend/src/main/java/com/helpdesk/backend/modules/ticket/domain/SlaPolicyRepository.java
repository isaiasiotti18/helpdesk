package com.helpdesk.backend.modules.ticket.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, UUID> {
    Optional<SlaPolicy> findByPriorityAndIsActiveTrue(Priority priority);
    List<SlaPolicy> findByIsActiveTrueOrderByPriorityAsc();
}
