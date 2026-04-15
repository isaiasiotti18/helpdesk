package com.helpdesk.backend.modules.chat.controllers;

import com.helpdesk.backend.modules.chat.application.dto.ChatSessionResponse;
import com.helpdesk.backend.modules.chat.application.dto.MessageResponse;
import com.helpdesk.backend.modules.chat.application.dto.SendMessageRequest;
import com.helpdesk.backend.modules.chat.application.dto.SendNoteRequest;
import com.helpdesk.backend.modules.chat.application.services.ChatMessageService;
import com.helpdesk.backend.modules.chat.application.services.ChatSessionService;
import com.helpdesk.backend.shared.dto.ApiResponse;
import com.helpdesk.backend.shared.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<ChatSessionResponse>> createSession(
            @RequestParam UUID ticketId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(chatSessionService.createForTicket(ticketId)));
    }

    @GetMapping("/sessions/by-ticket/{ticketId}")
    public ResponseEntity<ApiResponse<ChatSessionResponse>> getByTicket(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(ApiResponse.ok(chatSessionService.getByTicketId(ticketId)));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<ChatSessionResponse>> getSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(ApiResponse.ok(chatSessionService.getById(sessionId)));
    }

    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<Void> endSession(@PathVariable UUID sessionId) {
        chatSessionService.endSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    // Alterar getMessages para incluir flag
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessages(
            @PathVariable UUID sessionId,
            @RequestParam(defaultValue = "false") boolean includeInternal,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(chatMessageService.getMessages(sessionId, includeInternal, pageable)));
    }

    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            @CurrentUser UUID senderId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(chatMessageService.sendSync(request.sessionId(), senderId, request.content())));
    }

    @GetMapping("/sessions/{sessionId}/messages/cursor")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessagesCursor(
            @PathVariable UUID sessionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before,
            @RequestParam(defaultValue = "false") boolean includeInternal) {
        return ResponseEntity.ok(ApiResponse.ok(chatMessageService.getMessagesCursor(sessionId, before, includeInternal)));
    }

    @PostMapping("/notes")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<MessageResponse>> sendNote(
            @Valid @RequestBody SendNoteRequest request,
            @CurrentUser UUID senderId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(chatMessageService.sendNote(request.sessionId(), senderId, request.content())));
    }
}
