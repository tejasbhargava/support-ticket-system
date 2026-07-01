package com.tejas.ticketingsystem.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalTickets;
    private long openTickets;
    private long resolvedToday;
    private long highPriorityTickets;
    private double avgResolutionTimeHours;
    private Map<String, Long> ticketsByCategory;
    private Map<String, Long> ticketsByStatus;
}