package com.helpdesk.backend.modules.queue.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QueueRepository extends JpaRepository<Queue, UUID> {
    boolean existsByName(String name);
}
