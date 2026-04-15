package com.helpdesk.backend.modules.ticket.controllers;

import com.helpdesk.backend.modules.ticket.application.dtos.SlaPolicyResponse;
import com.helpdesk.backend.modules.ticket.domain.SlaPolicyRepository;
import com.helpdesk.backend.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sla-policies")
@RequiredArgsConstructor
public class SlaController {

    private final SlaPolicyRepository slaPolicyRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SlaPolicyResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(
                slaPolicyRepository.findByIsActiveTrueOrderByPriorityAsc().stream()
                        .map(SlaPolicyResponse::from)
                        .toList()
        ));
    }
}
