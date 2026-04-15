package com.helpdesk.backend.modules.chat.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CannedResponseRepository extends JpaRepository<CannedResponse, UUID> {

    @Query("""
        SELECT c FROM CannedResponse c
        WHERE c.isShared = true OR c.createdBy.id = :userId
        ORDER BY c.title ASC
    """)
    List<CannedResponse> findAccessibleByUser(@Param("userId") UUID userId);

    List<CannedResponse> findByCreatedByIdOrderByTitleAsc(UUID userId);

    @Query("""
        SELECT c FROM CannedResponse c
        WHERE (c.isShared = true OR c.createdBy.id = :userId)
        AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(c.content) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(c.shortcut) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY c.title ASC
    """)
    List<CannedResponse> search(@Param("userId") UUID userId, @Param("query") String query);
}
