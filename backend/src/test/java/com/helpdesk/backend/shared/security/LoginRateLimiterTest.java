package com.helpdesk.backend.shared.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterTest {

    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new LoginRateLimiter();
    }

    @Test
    @DisplayName("Não deve bloquear na primeira tentativa")
    void shouldNotBlockFirstAttempt() {
        assertThat(rateLimiter.isBlocked("test@email.com")).isFalse();
    }

    @Test
    @DisplayName("Não deve bloquear após 4 tentativas falhadas")
    void shouldNotBlockAfterFourFailures() {
        for (int i = 0; i < 4; i++) {
            rateLimiter.recordFailure("test@email.com");
        }
        assertThat(rateLimiter.isBlocked("test@email.com")).isFalse();
    }

    @Test
    @DisplayName("Deve bloquear após 5 tentativas falhadas")
    void shouldBlockAfterFiveFailures() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.recordFailure("test@email.com");
        }
        assertThat(rateLimiter.isBlocked("test@email.com")).isTrue();
    }

    @Test
    @DisplayName("Deve resetar contador após login com sucesso")
    void shouldResetAfterSuccess() {
        for (int i = 0; i < 4; i++) {
            rateLimiter.recordFailure("test@email.com");
        }
        rateLimiter.recordSuccess("test@email.com");
        assertThat(rateLimiter.isBlocked("test@email.com")).isFalse();
    }

    @Test
    @DisplayName("Deve isolar contadores por email")
    void shouldIsolateByKey() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.recordFailure("blocked@email.com");
        }
        assertThat(rateLimiter.isBlocked("blocked@email.com")).isTrue();
        assertThat(rateLimiter.isBlocked("other@email.com")).isFalse();
    }

    @Test
    @DisplayName("Deve continuar bloqueado após mais tentativas")
    void shouldStayBlockedAfterMoreAttempts() {
        for (int i = 0; i < 10; i++) {
            rateLimiter.recordFailure("test@email.com");
        }
        assertThat(rateLimiter.isBlocked("test@email.com")).isTrue();
    }
}