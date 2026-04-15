package com.helpdesk.backend.modules.chat.controllers;

import com.helpdesk.backend.modules.chat.application.services.CannedResponseService;
import com.helpdesk.backend.modules.chat.application.dto.CannedResponseDto;
import com.helpdesk.backend.modules.chat.application.dto.CreateCannedRequest;
import com.helpdesk.backend.modules.chat.application.dto.UpdateCannedRequest;
import com.helpdesk.backend.shared.dto.ApiResponse;
import com.helpdesk.backend.shared.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/canned-responses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
public class CannedResponseController {

    private final CannedResponseService cannedService;

    @PostMapping
    public ResponseEntity<ApiResponse<CannedResponseDto>> create(
            @Valid @RequestBody CreateCannedRequest request,
            @CurrentUser UUID userId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(cannedService.create(request, userId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CannedResponseDto>>> list(@CurrentUser UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(cannedService.listAccessible(userId)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CannedResponseDto>>> search(
            @CurrentUser UUID userId,
            @RequestParam String q
    ) {
        return ResponseEntity.ok(ApiResponse.ok(cannedService.search(userId, q)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CannedResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCannedRequest request,
            @CurrentUser UUID userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(cannedService.update(id, request, userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @CurrentUser UUID userId) {
        cannedService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
