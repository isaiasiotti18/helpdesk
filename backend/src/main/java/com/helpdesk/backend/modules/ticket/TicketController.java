package com.helpdesk.backend.modules.ticket;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.backend.modules.ticket.domain.Priority;
import com.helpdesk.backend.modules.ticket.domain.TicketStatus;
import com.helpdesk.backend.modules.ticket.dtos.AssignRequest;
import com.helpdesk.backend.modules.ticket.dtos.CreateTicketRequest;
import com.helpdesk.backend.modules.ticket.dtos.TicketResponse;
import com.helpdesk.backend.modules.ticket.dtos.UpdateStatusRequest;
import com.helpdesk.backend.modules.ticket.services.TicketAssignmentService;
import com.helpdesk.backend.modules.ticket.services.TicketLifecycleService;
import com.helpdesk.backend.modules.ticket.services.TicketService;
import com.helpdesk.backend.shared.dto.ApiResponse;
import com.helpdesk.backend.shared.security.CurrentUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;
    private final TicketAssignmentService assignmentService;
    private final TicketLifecycleService lifecycleService;

    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> create(
            @Valid @RequestBody CreateTicketRequest request,
            @CurrentUser UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(ticketService.create(request, userId)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> list(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) UUID agentId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(ticketService.list(status, priority, agentId, pageable)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> myTickets(
            @CurrentUser UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(ticketService.myTickets(userId, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> getById(
            @PathVariable UUID id,
            @CurrentUser UUID userId) {
        String role = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return ResponseEntity.ok(ApiResponse.ok(ticketService.getById(id, userId, role)));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<TicketResponse>> assign(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                assignmentService.assign(id, UUID.fromString(request.agentId()))));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<TicketResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(lifecycleService.updateStatus(id, request.status())));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<TicketResponse>> close(
            @PathVariable UUID id,
            @CurrentUser UUID userId) {
        String role = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return ResponseEntity.ok(ApiResponse.ok(ticketService.close(id, userId, role)));
    }
}
