package com.helpdesk.backend.modules.ticket;

import com.fasterxml.jackson.databind.JsonNode;
import com.helpdesk.backend.BaseIntegrationTest;
import com.helpdesk.backend.modules.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TicketOwnershipIntegrationTest extends BaseIntegrationTest {

        @Autowired
        private UserRepository userRepository;

        private String clientAToken;
        private String clientBToken;
        private String agentToken;
        private String ticketIdByClientA;

        @BeforeAll
        void setupUsers() throws Exception {
                // Registrar Client A
                var resultA = mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"name":"Client A","email":"clienta@test.com","password":"123456"}
                                                """))
                                .andReturn();
                clientAToken = extractToken(resultA.getResponse().getContentAsString(), "accessToken");

                // Registrar Client B
                var resultB = mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"name":"Client B","email":"clientb@test.com","password":"123456"}
                                                """))
                                .andReturn();
                clientBToken = extractToken(resultB.getResponse().getContentAsString(), "accessToken");

                // Registrar Agent e mudar role no banco
                var resultAgent = mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"name":"Agent","email":"agent-own@test.com","password":"123456"}
                                                """))
                                .andReturn();

                var agentUser = userRepository.findByEmail("agent-own@test.com").orElseThrow();
                agentUser.setRole(com.helpdesk.backend.modules.user.domain.Role.AGENT);
                userRepository.save(agentUser);

                // Re-login como agent pra pegar token com role correto
                var loginResult = mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"email":"agent-own@test.com","password":"123456"}
                                                """))
                                .andReturn();
                agentToken = extractToken(loginResult.getResponse().getContentAsString(), "accessToken");

                // Client A cria ticket
                var ticketResult = mockMvc.perform(post("/tickets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + clientAToken)
                                .content("""
                                                {"title":"Ticket do Client A","priority":"HIGH"}
                                                """))
                                .andReturn();
                JsonNode ticketJson = objectMapper.readTree(ticketResult.getResponse().getContentAsString());
                ticketIdByClientA = ticketJson.get("data").get("id").asText();
        }

        @Test
        @DisplayName("Client A deve acessar seu próprio ticket")
        void clientAShouldAccessOwnTicket() throws Exception {
                mockMvc.perform(get("/tickets/" + ticketIdByClientA)
                                .header("Authorization", "Bearer " + clientAToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.title").value("Ticket do Client A"));
        }

        @Test
        @DisplayName("Client B NÃO deve acessar ticket do Client A")
        void clientBShouldNotAccessClientATicket() throws Exception {
                mockMvc.perform(get("/tickets/" + ticketIdByClientA)
                                .header("Authorization", "Bearer " + clientBToken))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Agent deve acessar ticket de qualquer client")
        void agentShouldAccessAnyTicket() throws Exception {
                mockMvc.perform(get("/tickets/" + ticketIdByClientA)
                                .header("Authorization", "Bearer " + agentToken))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Client B NÃO deve fechar ticket do Client A")
        void clientBShouldNotCloseClientATicket() throws Exception {
                mockMvc.perform(post("/tickets/" + ticketIdByClientA + "/close")
                                .header("Authorization", "Bearer " + clientBToken))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Client A deve fechar seu próprio ticket")
        void clientAShouldCloseOwnTicket() throws Exception {
                // Primeiro precisa assign pra poder fechar (OPEN → IN_PROGRESS → CLOSED)
                // Assign via agent
                mockMvc.perform(post("/tickets/" + ticketIdByClientA + "/assign")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + agentToken)
                                .content("""
                                                {"agentId":"%s"}
                                                """.formatted(
                                                userRepository.findByEmail("agent-own@test.com").orElseThrow()
                                                                .getId())))
                                .andExpect(status().isOk());

                // Client A fecha
                mockMvc.perform(post("/tickets/" + ticketIdByClientA + "/close")
                                .header("Authorization", "Bearer " + clientAToken))
                                .andExpect(status().isOk());
        }

}
