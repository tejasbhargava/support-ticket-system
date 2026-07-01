package com.tejas.ticketingsystem.service;

import com.tejas.ticketingsystem.dto.dashboard.AdminDashboardResponse;
import com.tejas.ticketingsystem.dto.dashboard.AgentDashboardResponse;
import com.tejas.ticketingsystem.dto.dashboard.CustomerDashboardResponse;
import com.tejas.ticketingsystem.dto.ticket.TicketSummaryResponse;
import com.tejas.ticketingsystem.entity.User;
import com.tejas.ticketingsystem.enums.Priority;
import com.tejas.ticketingsystem.enums.Status;
import com.tejas.ticketingsystem.mapper.TicketMapper;
import com.tejas.ticketingsystem.repository.TicketRepository;
import com.tejas.ticketingsystem.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public DashboardService(TicketRepository ticketRepository, TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
    }

    public Object getDashboard() {
        User currentUser = getCurrentUser();

        return switch (currentUser.getRole()) {
            case CUSTOMER -> getCustomerDashboard(currentUser);
            case AGENT -> getAgentDashboard(currentUser);
            case ADMIN -> getAdminDashboard();
        };
    }

    // ── CUSTOMER ────────────────────────────────────────────

    private CustomerDashboardResponse getCustomerDashboard(User user) {
        long open = ticketRepository.countByCreatedByAndStatus(user, Status.OPEN);
        long resolved = ticketRepository.countByCreatedByAndStatus(user, Status.RESOLVED);
        long waiting = ticketRepository.countByCreatedByAndStatus(user, Status.WAITING_FOR_CUSTOMER);

        // 5 most recent tickets
        List<TicketSummaryResponse> recent = ticketRepository
                .findByCreatedBy(user, org.springframework.data.domain.PageRequest.of(0, 5,
                        org.springframework.data.domain.Sort.by("createdAt").descending()))
                .map(ticketMapper::toSummaryResponse)
                .toList();

        return new CustomerDashboardResponse(open, resolved, waiting, recent);
    }

    // ── AGENT ───────────────────────────────────────────────

    private AgentDashboardResponse getAgentDashboard(User agent) {
        long assigned = ticketRepository.countByAssignedAgentAndStatus(agent, Status.OPEN) +
                ticketRepository.countByAssignedAgentAndStatus(agent, Status.IN_PROGRESS);

        long highPriority = ticketRepository
                .countByAssignedAgentAndPriority(agent, Priority.HIGH);

        long waiting = ticketRepository
                .countByAssignedAgentAndStatus(agent, Status.WAITING_FOR_CUSTOMER);

        // overdue — filter agent's open tickets
        long overdue = ticketRepository
                .findByAssignedAgentOrAssignedAgentIsNull(agent,
                        org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE,
                                org.springframework.data.domain.Sort.unsorted()))
                .stream()
                .filter(t -> t.getStatus() != Status.RESOLVED && t.getStatus() != Status.CLOSED)
                .filter(t -> {
                    LocalDateTime deadline = t.getCreatedAt()
                            .plusHours(t.getCategory().getDefaultSlaHours());
                    return LocalDateTime.now().isAfter(deadline);
                })
                .count();

        // 5 most recently assigned
        List<TicketSummaryResponse> recent = ticketRepository
                .findByAssignedAgentOrAssignedAgentIsNull(agent,
                        org.springframework.data.domain.PageRequest.of(0, 5,
                                org.springframework.data.domain.Sort.by("createdAt").descending()))
                .map(ticketMapper::toSummaryResponse)
                .toList();

        return new AgentDashboardResponse(assigned, highPriority, overdue, waiting, recent);
    }

    // ── ADMIN ───────────────────────────────────────────────

    private AdminDashboardResponse getAdminDashboard() {
        long total = ticketRepository.count();
        long open = ticketRepository.countByStatus(Status.OPEN);
        long highPriority = ticketRepository.countByPriority(Priority.HIGH);

        // resolved today
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long resolvedToday = ticketRepository.countResolvedToday(startOfDay);

        // avg resolution time
        Double avg = ticketRepository.avgResolutionTimeHours();
        double avgResolution = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;

        // tickets by category
        Map<String, Long> byCategory = new HashMap<>();
        ticketRepository.countByCategory()
                .forEach(row -> byCategory.put((String) row[0], (Long) row[1]));

        // tickets by status
        Map<String, Long> byStatus = new HashMap<>();
        ticketRepository.countGroupedByStatus()
                .forEach(row -> byStatus.put(row[0].toString(), (Long) row[1]));

        return new AdminDashboardResponse(
                total, open, resolvedToday,
                highPriority, avgResolution,
                byCategory, byStatus
        );
    }

    private User getCurrentUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return userDetails.getUser();
    }
}