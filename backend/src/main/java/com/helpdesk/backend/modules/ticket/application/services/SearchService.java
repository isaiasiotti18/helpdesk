package com.helpdesk.backend.modules.ticket.application.services;

import com.helpdesk.backend.modules.ticket.application.dtos.SearchResultResponse;
import com.helpdesk.backend.modules.ticket.domain.Ticket;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<SearchResultResponse> search(String query, int limit) {
        String sanitized = "%" + query.toLowerCase().replace("%", "").replace("_", "") + "%";

        TypedQuery<Ticket> jpql = entityManager.createQuery("""
                SELECT DISTINCT t FROM Ticket t
                LEFT JOIN FETCH t.createdBy
                LEFT JOIN FETCH t.assignedAgent
                LEFT JOIN FETCH t.category
                WHERE LOWER(t.title) LIKE :query
                   OR LOWER(t.description) LIKE :query
                ORDER BY t.createdAt DESC
                """, Ticket.class)
                .setParameter("query", sanitized)
                .setMaxResults(limit);

        return jpql.getResultList().stream()
                .map(SearchResultResponse::from)
                .toList();
    }
}
