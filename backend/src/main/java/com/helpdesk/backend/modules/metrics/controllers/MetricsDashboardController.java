package com.helpdesk.backend.modules.metrics.controllers;

import com.helpdesk.backend.modules.metrics.application.dtos.DashboardMetricsResponse;
import com.helpdesk.backend.modules.metrics.application.services.MetricsDashboardService;
import com.helpdesk.backend.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MetricsDashboardController {

    private final MetricsDashboardService metricsService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok(metricsService.getDashboard()));
    }
}
