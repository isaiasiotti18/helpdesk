package com.helpdesk.backend.modules.metrics.application.dtos;

import java.util.List;

public record DashboardMetricsResponse(
        TicketCounts ticketCounts,
        PerformanceMetrics performance,
        List<AgentLoad> agentLoad,
        List<DailyCount> ticketsPerDay
) {
    public record TicketCounts(
            long open,
            long inProgress,
            long resolved,
            long closed,
            long slaBbreached,
            long total
    ) {}

    public record PerformanceMetrics(
            Double avgFirstResponseMinutes,
            Double avgResolutionMinutes,
            Double slaCompliancePercent
    ) {}

    public record AgentLoad(
            String agentId,
            String agentName,
            long activeTickets,
            long resolvedToday
    ) {}

    public record DailyCount(
            String date,
            long count
    ) {}
}
