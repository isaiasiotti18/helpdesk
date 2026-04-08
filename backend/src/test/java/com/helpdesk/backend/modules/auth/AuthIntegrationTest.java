package com.helpdesk.backend.modules.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpdesk.backend.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest extends BaseIntegrationTest {

        private static String accessToken;
        private static String refreshToken;

        @Test
        @Order(1)
        @DisplayName("Deve registrar novo usuário")
        void shouldRegister() throws Exception {
                String body = """
                                {"name":"Test User","email":"auth-test@test.com","password":"123456"}
                                """;

                var result = mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                                .andExpect(jsonPath("$.data.user.email").value("auth-test@test.com"))
                                .andReturn();

                JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
                accessToken = json.get("data").get("accessToken").asText();
                refreshToken = json.get("data").get("refreshToken").asText();
        }

        @Test
        @Order(2)
        @DisplayName("Deve logar com credenciais válidas")
        void shouldLogin() throws Exception {
                String body = """
                                {"email":"auth-test@test.com","password":"123456"}
                                """;

                var result = mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                                .andReturn();

                JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
                accessToken = json.get("data").get("accessToken").asText();
                refreshToken = json.get("data").get("refreshToken").asText();
        }

        @Test
        @Order(3)
        @DisplayName("Deve retornar dados do usuário logado")
        void shouldReturnMe() throws Exception {
                mockMvc.perform(get("/auth/me")
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.email").value("auth-test@test.com"));
        }

        @Test
        @Order(4)
        @DisplayName("Deve fazer refresh e revogar token antigo")
        void shouldRefreshAndRevokeOld() throws Exception {
                String body = """
                                {"refreshToken":"%s"}
                                """.formatted(refreshToken);

                // Primeiro refresh — deve funcionar
                var result = mockMvc.perform(post("/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                                .andReturn();

                // Segundo refresh com mesmo token — deve falhar (revogado)
                mockMvc.perform(post("/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isUnauthorized());

                // Salvar novo token pro próximo teste
                JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
                accessToken = json.get("data").get("accessToken").asText();
                refreshToken = json.get("data").get("refreshToken").asText();
        }

        @Test
        @Order(5)
        @DisplayName("Deve revogar todos os tokens no logout")
        void shouldRevokeAllOnLogout() throws Exception {
                // Logout
                mockMvc.perform(post("/auth/logout")
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isNoContent());

                // Tentar refresh com token antigo — deve falhar
                String body = """
                                {"refreshToken":"%s"}
                                """.formatted(refreshToken);

                mockMvc.perform(post("/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @Order(6)
        @DisplayName("Deve rejeitar credenciais inválidas")
        void shouldRejectInvalidCredentials() throws Exception {
                String body = """
                                {"email":"auth-test@test.com","password":"wrongpassword"}
                                """;

                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").value("Invalid credentials"));
        }

        @Test
        @Order(7)
        @DisplayName("Deve bloquear após 5 tentativas falhadas")
        void shouldBlockAfterFiveFailedAttempts() throws Exception {
                String body = """
                                {"email":"ratelimit@test.com","password":"wrong"}
                                """;

                // Registrar o usuário primeiro
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"name":"Rate User","email":"ratelimit@test.com","password":"123456"}
                                                """))
                                .andExpect(status().isCreated());

                // 5 tentativas falhadas
                for (int i = 0; i < 5; i++) {
                        mockMvc.perform(post("/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isUnauthorized());
                }

                // 6ª tentativa — deve retornar 429
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isTooManyRequests())
                                .andExpect(jsonPath("$.error")
                                                .value("Too many login attempts. Try again in 5 minutes."));

                // Mesmo com senha correta — bloqueado
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"email":"ratelimit@test.com","password":"123456"}
                                                """))
                                .andExpect(status().isTooManyRequests());
        }
}
