package com.helpdesk.backend.modules.ticket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import com.helpdesk.backend.modules.category.domain.Category;
import com.helpdesk.backend.modules.user.domain.Role;
import com.helpdesk.backend.modules.user.domain.User;
import com.helpdesk.backend.shared.exception.BusinessException;

@Entity
@Table(name = "tickets")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent")
    private User assignedAgent;

    @Column(name = "queue_id")
    private UUID queueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Version
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Ticket create(String title, String description, Priority priority, User createdBy) {
        return Ticket.builder()
                .title(title)
                .description(description)
                .priority(priority)
                .status(TicketStatus.OPEN)
                .createdBy(createdBy)
                .build();
    }

    public void assignTo(User agent) {
        if (agent.getRole() != Role.AGENT && agent.getRole() != Role.ADMIN) {
            throw new BusinessException("Only AGENT or ADMIN can be assigned to tickets");
        }
        this.assignedAgent = agent;
        transitionTo(TicketStatus.IN_PROGRESS);
    }

    public void transitionTo(TicketStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Cannot transition from " + this.status + " to " + newStatus);
        }
        this.status = newStatus;
        if (newStatus == TicketStatus.CLOSED) {
            this.closedAt = LocalDateTime.now();
        }
    }

    public void close() {
        transitionTo(TicketStatus.CLOSED);
    }

    public void resolve() {
        transitionTo(TicketStatus.RESOLVED);
    }

    public void transfer(User newAgent) {
        transitionTo(TicketStatus.TRANSFERRED);
        this.assignedAgent = newAgent;
        this.status = TicketStatus.IN_PROGRESS;
    }

    public void setAssignedAgent(User agent) {
        this.assignedAgent = agent;
    }

    public void setQueueId(UUID queueId) {
        this.queueId = queueId;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
