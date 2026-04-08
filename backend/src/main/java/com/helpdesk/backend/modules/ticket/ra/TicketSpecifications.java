package com.helpdesk.backend.modules.ticket.ra;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.helpdesk.backend.modules.ticket.domain.Priority;
import com.helpdesk.backend.modules.ticket.domain.Ticket;
import com.helpdesk.backend.modules.ticket.domain.TicketStatus;

import jakarta.persistence.criteria.JoinType;

// O que faz: cada método retorna uma Specification que adiciona uma condição ao WHERE.
// Retornar null significa "sem filtro". fetchRelations() resolve o N+1 pra queries via Specification.
// O check Long.class != query.getResultType() evita fetch no count query da paginação.

public final class TicketSpecifications {

    private TicketSpecifications() {
    }

    public static Specification<Ticket> withStatus(TicketStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Ticket> withPriority(Priority priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Ticket> withAssignedAgent(UUID agentId) {
        return (root, query, cb) -> agentId == null ? null : cb.equal(root.get("assignedAgent").get("id"), agentId);
    }

    public static Specification<Ticket> withCreatedBy(UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("createdBy").get("id"), userId);
    }

    public static Specification<Ticket> fetchRelations() {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType()) {
                root.fetch("createdBy", JoinType.LEFT);
                root.fetch("assignedAgent", JoinType.LEFT);
            }
            return null;
        };
    }
}
