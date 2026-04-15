package com.helpdesk.backend.modules.ticket.controllers;

import com.helpdesk.backend.modules.ticket.application.dtos.RatingRequest;
import com.helpdesk.backend.modules.ticket.application.dtos.RatingResponse;
import com.helpdesk.backend.modules.ticket.application.services.RatingService;
import com.helpdesk.backend.shared.dto.ApiResponse;
import com.helpdesk.backend.shared.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tickets/{ticketId}/rating")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<ApiResponse<RatingResponse>> rate(
            @PathVariable UUID ticketId,
            @Valid @RequestBody RatingRequest request,
            @CurrentUser UUID userId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(ratingService.rate(ticketId, request, userId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<RatingResponse>> get(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(ApiResponse.ok(ratingService.getByTicketId(ticketId)));
    }
}
