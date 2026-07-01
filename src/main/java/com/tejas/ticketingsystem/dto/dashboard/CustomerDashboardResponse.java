package com.tejas.ticketingsystem.dto.dashboard;

import com.tejas.ticketingsystem.dto.ticket.TicketSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDashboardResponse {
    private long openTickets;
    private long resolvedTickets;
    private long waitingForCustomer;
    private List<TicketSummaryResponse> recentTickets;
}