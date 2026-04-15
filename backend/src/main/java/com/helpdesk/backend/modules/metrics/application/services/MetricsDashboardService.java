package com.helpdesk.backend.modules.metrics.application.services;

import com.helpdesk.backend.modules.metrics.application.dtos.DashboardMetricsResponse;
import com.helpdesk.backend.modules.metrics.application.dtos.DashboardMetricsResponse.*;
import com.helpdesk.backend.modules.ticket.domain.Ticket;
import com.helpdesk.backend.modules.ticket.domain.TicketStatus;
import com.helpdesk.backend.modules.ticket.domain.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricsDashboardService {

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public DashboardMetricsResponse getDashboard() {
        List<Ticket> allTickets = ticketRepository.findAll();

        TicketCounts counts = buildCounts(allTickets);
        PerformanceMetrics performance = buildPerformance(allTickets);
        List<AgentLoad> agentLoad = buildAgentLoad(allTickets);
        List<DailyCount> ticketsPerDay = buildDailyCount(allTickets);

        return new DashboardMetricsResponse(counts, performance, agentLoad, ticketsPerDay);
    }

    private TicketCounts buildCounts(List<Ticket> tickets) {
        Map<TicketStatus, Long> byStatus = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getStatus, Collectors.counting()));

        long breached = tickets.stream().filter(t -> Boolean.TRUE.equals(t.getSlaBbreached())).count();

        return new TicketCounts(
                byStatus.getOrDefault(TicketStatus.OPEN, 0L),
                byStatus.getOrDefault(TicketStatus.IN_PROGRESS, 0L),
                byStatus.getOrDefault(TicketStatus.RESOLVED, 0L),
                byStatus.getOrDefault(TicketStatus.CLOSED, 0L),
                breached,
                tickets.size()
        );
    }

    private PerformanceMetrics buildPerformance(List<Ticket> tickets) {
        OptionalDouble avgFr = tickets.stream()
                .filter(t -> t.getFirstResponseAt() != null)
                .mapToLong(t -> ChronoUnit.MINUTES.between(t.getCreatedAt(), t.getFirstResponseAt()))
                .average();

        OptionalDouble avgRes = tickets.stream()
                .filter(t -> t.getClosedAt() != null)
                .mapToLong(t -> ChronoUnit.MINUTES.between(t.getCreatedAt(), t.getClosedAt()))
                .average();

        long withSla = tickets.stream().filter(t -> t.getSlaResolutionDeadline() != null).count();
        long compliant = tickets.stream()
                .filter(t -> t.getSlaResolutionDeadline() != null)
                .filter(t -> !Boolean.TRUE.equals(t.getSlaBbreached()))
                .count();

        Double slaPercent = withSla > 0 ? (compliant * 100.0 / withSla) : null;

        return new PerformanceMetrics(
                avgFr.isPresent() ? avgFr.getAsDouble() : null,
                avgRes.isPresent() ? avgRes.getAsDouble() : null,
                slaPercent
        );
    }

    private List<AgentLoad> buildAgentLoad(List<Ticket> tickets) {
        LocalDate today = LocalDate.now();

        Map<UUID, List<Ticket>> byAgent = tickets.stream()
                .filter(t -> t.getAssignedAgent() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignedAgent().getId()));

        return byAgent.entrySet().stream()
                .map(entry -> {
                    List<Ticket> agentTickets = entry.getValue();
                    String agentName = agentTickets.get(0).getAssignedAgent().getName();

                    long active = agentTickets.stream()
                            .filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS)
                            .count();

                    long resolvedToday = agentTickets.stream()
                            .filter(t -> t.getClosedAt() != null && t.getClosedAt().toLocalDate().equals(today))
                            .count();

                    return new AgentLoad(entry.getKey().toString(), agentName, active, resolvedToday);
                })
                .sorted(Comparator.comparingLong(AgentLoad::activeTickets).reversed())
                .toList();
    }

    private List<DailyCount> buildDailyCount(List<Ticket> tickets) {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(29);

        Map<LocalDate, Long> byDay = tickets.stream()
                .filter(t -> t.getCreatedAt().toLocalDate().isAfter(from.minusDays(1)))
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().toLocalDate(),
                        Collectors.counting()
                ));

        List<DailyCount> result = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(today); date = date.plusDays(1)) {
            result.add(new DailyCount(date.toString(), byDay.getOrDefault(date, 0L)));
        }

        return result;
    }
}
