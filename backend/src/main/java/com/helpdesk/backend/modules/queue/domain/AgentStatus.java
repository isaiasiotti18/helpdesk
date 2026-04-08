package com.helpdesk.backend.modules.queue.domain;

import com.helpdesk.backend.modules.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentStatus {

    @Id
    @Column(name = "agent_id")
    private UUID agentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "agent_id")
    private User agent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AgentOnlineStatus status = AgentOnlineStatus.OFFLINE;

    @Column(name = "active_tickets", nullable = false)
    @Builder.Default
    private Integer activeTickets = 0;

    @Column(name = "max_tickets", nullable = false)
    @Builder.Default
    private Integer maxTickets = 5;

    @Column(name = "last_seen", nullable = false)
    @Builder.Default
    private LocalDateTime lastSeen = LocalDateTime.now();

    public boolean isAvailable() {
        return status == AgentOnlineStatus.ONLINE && activeTickets < maxTickets;
    }

    public void incrementActiveTickets() {
        this.activeTickets++;
        if (this.activeTickets >= this.maxTickets) {
            this.status = AgentOnlineStatus.BUSY;
        }
    }

    public void decrementActiveTickets() {
        this.activeTickets = Math.max(0, this.activeTickets - 1);
        if (this.status == AgentOnlineStatus.BUSY && this.activeTickets < this.maxTickets) {
            this.status = AgentOnlineStatus.ONLINE;
        }
    }

    public void goOnline() {
        this.status = AgentOnlineStatus.ONLINE;
        this.lastSeen = LocalDateTime.now();
    }

    public void goOffline() {
        this.status = AgentOnlineStatus.OFFLINE;
        this.lastSeen = LocalDateTime.now();
    }
}
