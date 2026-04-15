package com.helpdesk.backend.modules.ticket.controllers;

import com.helpdesk.backend.modules.ticket.application.dtos.SearchResultResponse;
import com.helpdesk.backend.modules.ticket.application.services.SearchService;
import com.helpdesk.backend.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SearchResultResponse>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.ok(searchService.search(q, Math.min(limit, 50))));
    }
}
