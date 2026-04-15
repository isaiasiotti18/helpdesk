package com.helpdesk.backend.modules.category.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByIsActiveTrueOrderByNameAsc();
    boolean existsByName(String name);
    List<Category> findByQueueId(UUID queueId);
}
