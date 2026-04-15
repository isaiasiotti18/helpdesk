package com.helpdesk.backend.modules.ticket.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.backend.modules.ticket.application.dtos.ActivityResponse;
import com.helpdesk.backend.modules.ticket.application.services.TicketActivityService;
import com.helpdesk.backend.shared.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tickets/{ticketId}/activities")
@RequiredArgsConstructor
public class TicketActivityController {

    private final TicketActivityService activityService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getActivities(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(ApiResponse.ok(activityService.getByTicketId(ticketId)));
    }
}
