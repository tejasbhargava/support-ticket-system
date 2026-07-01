package com.tejas.ticketingsystem.dto.dashboard;

import com.tejas.ticketingsystem.dto.ticket.TicketSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDashboardResponse {
    private long assignedTickets;
    private long highPriorityTickets;
    private long overdueTickets;
    private long waitingForCustomer;
    private List<TicketSummaryResponse> recentAssignments;
}