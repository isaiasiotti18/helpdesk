package com.helpdesk.backend.modules.chat.controllers;

import com.helpdesk.backend.modules.chat.application.dto.ChatMessagePayload;
import com.helpdesk.backend.modules.chat.application.dto.MessageResponse;
import com.helpdesk.backend.modules.chat.application.services.ChatMessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketHandler {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Cliente envia para: /app/chat.send
     * Servidor broadcast para: /topic/chat.{sessionId}
     */
    @MessageMapping("chat.send")
    public void handleMessage(ChatMessagePayload payload, Principal principal) {
        UUID senderId = UUID.fromString(principal.getName());
        UUID sessionId = UUID.fromString(payload.sessionId());

        MessageResponse response = chatMessageService.sendAsync(sessionId, senderId, payload.content());

        messagingTemplate.convertAndSend(
                "/topic/chat." + payload.sessionId(),
                response);
    }

    @MessageMapping("chat.note")
    public void handleNote(ChatMessagePayload payload, Principal principal) {
        UUID senderId = UUID.fromString(principal.getName());
        UUID sessionId = UUID.fromString(payload.sessionId());

        MessageResponse response = chatMessageService.sendNote(sessionId, senderId, payload.content());

        // Broadcast só no canal de notas internas (agentes subscrevem neste tópico)
        messagingTemplate.convertAndSend(
                "/topic/chat." + payload.sessionId() + ".notes",
                response);
    }
}
