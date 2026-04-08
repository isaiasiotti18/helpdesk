package com.helpdesk.backend.modules.queue.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.helpdesk.backend.modules.queue.application.dto.AgentStatusResponse;
import com.helpdesk.backend.modules.queue.application.dto.UpdateAgentStatusRequest;
import com.helpdesk.backend.modules.queue.application.services.AgentStatusService;
import com.helpdesk.backend.modules.queue.application.services.AssignmentService;
import com.helpdesk.backend.shared.dto.ApiResponse;
import com.helpdesk.backend.shared.security.CurrentUser;

import java.util.UUID;

@RestController
@RequestMapping("/agents")
@RequiredArgsConstructor
public class AgentStatusController {

    private final AgentStatusService agentStatusService;
    private final AssignmentService assignmentService;

    @PatchMapping("/me/status")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<AgentStatusResponse>> updateMyStatus(
            @CurrentUser UUID agentId,
            @Valid @RequestBody UpdateAgentStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(agentStatusService.updateStatus(agentId, request.status())));
    }

    @GetMapping("/{agentId}/status")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<AgentStatusResponse>> getStatus(@PathVariable UUID agentId) {
        return ResponseEntity.ok(ApiResponse.ok(agentStatusService.getStatus(agentId)));
    }

    @PostMapping("/assign/{ticketId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> manualAssign(@PathVariable UUID ticketId) {
        return assignmentService.autoAssign(ticketId)
                .map(agentId -> ResponseEntity.ok(ApiResponse.ok("Assigned to " + agentId)))
                .orElse(ResponseEntity.ok(ApiResponse.ok("No available agent")));
    }
}
