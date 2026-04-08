package com.helpdesk.backend.modules.queue.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.helpdesk.backend.modules.queue.application.dto.CreateQueueRequest;
import com.helpdesk.backend.modules.queue.application.dto.QueueResponse;
import com.helpdesk.backend.modules.queue.application.services.QueueService;
import com.helpdesk.backend.shared.dto.ApiResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/queues")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class QueueController {

    private final QueueService queueService;

    @PostMapping
    public ResponseEntity<ApiResponse<QueueResponse>> create(@Valid @RequestBody CreateQueueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(queueService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<QueueResponse>>> listAll() {
        return ResponseEntity.ok(ApiResponse.ok(queueService.listAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QueueResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(queueService.getById(id)));
    }

    @PostMapping("/{queueId}/agents/{agentId}")
    public ResponseEntity<Void> addAgent(@PathVariable UUID queueId, @PathVariable UUID agentId) {
        queueService.addAgent(queueId, agentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{queueId}/agents/{agentId}")
    public ResponseEntity<Void> removeAgent(@PathVariable UUID queueId, @PathVariable UUID agentId) {
        queueService.removeAgent(queueId, agentId);
        return ResponseEntity.noContent().build();
    }
}
