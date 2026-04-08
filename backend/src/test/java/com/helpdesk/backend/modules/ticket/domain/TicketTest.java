package com.helpdesk.backend.modules.ticket.domain;

import com.helpdesk.backend.modules.user.domain.Role;
import com.helpdesk.backend.modules.user.domain.User;
import com.helpdesk.backend.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketTest {

    private User client;
    private User agent;
    private User admin;
    private User otherClient;

    @BeforeEach
    void setUp() {
        client = User.builder()
                .id(UUID.randomUUID())
                .name("Client")
                .email("client@test.com")
                .passwordHash("hash")
                .role(Role.CLIENT)
                .build();

        otherClient = User.builder()
                .id(UUID.randomUUID())
                .name("Other Client")
                .email("other@test.com")
                .passwordHash("hash")
                .role(Role.CLIENT)
                .build();

        agent = User.builder()
                .id(UUID.randomUUID())
                .name("Agent")
                .email("agent@test.com")
                .passwordHash("hash")
                .role(Role.AGENT)
                .build();

        admin = User.builder()
                .id(UUID.randomUUID())
                .name("Admin")
                .email("admin@test.com")
                .passwordHash("hash")
                .role(Role.ADMIN)
                .build();
    }

    @Test
    @DisplayName("Deve criar ticket com status OPEN")
    void shouldCreateWithOpenStatus() {
        Ticket ticket = Ticket.create("Teste", "Desc", Priority.MEDIUM, client);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(ticket.getCreatedBy()).isEqualTo(client);
    }

    @Test
    @DisplayName("Deve atribuir ticket a AGENT")
    void shouldAssignToAgent() {
        Ticket ticket = Ticket.create("Teste", "Desc", Priority.MEDIUM, client);
        ticket.assignTo(agent);
        assertThat(ticket.getAssignedAgent()).isEqualTo(agent);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Deve atribuir ticket a ADMIN")
    void shouldAssignToAdmin() {
        Ticket ticket = Ticket.create("Teste", "Desc", Priority.MEDIUM, client);
        ticket.assignTo(admin);
        assertThat(ticket.getAssignedAgent()).isEqualTo(admin);
    }

    @Test
    @DisplayName("Não deve atribuir ticket a CLIENT")
    void shouldNotAssignToClient() {
        Ticket ticket = Ticket.create("Teste", "Desc", Priority.MEDIUM, client);
        assertThatThrownBy(() -> ticket.assignTo(otherClient))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only AGENT or ADMIN");
    }

    @Test
    @DisplayName("Deve fechar ticket")
    void shouldCloseTicket() {
        Ticket ticket = Ticket.create("Teste", "Desc", Priority.MEDIUM, client);
        ticket.assignTo(agent);
        ticket.close();
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CLOSED);
        assertThat(ticket.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("Não deve fechar ticket já fechado")
    void shouldNotCloseAlreadyClosedTicket() {
        Ticket ticket = Ticket.create("Teste", "Desc", Priority.MEDIUM, client);
        ticket.assignTo(agent);
        ticket.close();
        assertThatThrownBy(() -> ticket.close())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deve resolver ticket em progresso")
    void shouldResolveInProgressTicket() {
        Ticket ticket = Ticket.create("Teste", "Desc", Priority.MEDIUM, client);
        ticket.assignTo(agent);
        ticket.resolve();
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.RESOLVED);
    }

    @Test
    @DisplayName("Não deve resolver ticket OPEN")
    void shouldNotResolveOpenTicket() {
        Ticket ticket = Ticket.create("Teste", "Desc", Priority.MEDIUM, client);
        assertThatThrownBy(() -> ticket.resolve())
                .isInstanceOf(IllegalStateException.class);
    }
}