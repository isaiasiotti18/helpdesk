package com.helpdesk.backend.modules.ticket.controllers;

import com.helpdesk.backend.modules.ticket.application.dtos.TicketResponse;
import com.helpdesk.backend.modules.ticket.application.services.TransferService;
import com.helpdesk.backend.modules.ticket.application.dtos.TransferRequest;
import com.helpdesk.backend.shared.dto.ApiResponse;
import com.helpdesk.backend.shared.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tickets/{ticketId}")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/transfer-agent")
    public ResponseEntity<ApiResponse<TicketResponse>> transferToAgent(
            @PathVariable UUID ticketId,
            @Valid @RequestBody TransferRequest request,
            @CurrentUser UUID userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                transferService.transferToAgent(ticketId, request.agentId(), userId, request.reason())
        ));
    }

    @PostMapping("/transfer-queue")
    public ResponseEntity<ApiResponse<TicketResponse>> transferToQueue(
            @PathVariable UUID ticketId,
            @Valid @RequestBody TransferRequest request,
            @CurrentUser UUID userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                transferService.transferToQueue(ticketId, request.queueId(), userId, request.reason())
        ));
    }
}
