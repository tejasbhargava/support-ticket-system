package com.tejas.ticketingsystem.mapper;

import com.tejas.ticketingsystem.dto.ticket.TicketResponse;
import com.tejas.ticketingsystem.dto.ticket.TicketSummaryResponse;
import com.tejas.ticketingsystem.entity.Ticket;
import com.tejas.ticketingsystem.enums.Status;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TicketMapper {

    public TicketResponse toResponse(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setSubject(ticket.getSubject());
        response.setDescription(ticket.getDescription());
        response.setStatus(ticket.getStatus());
        response.setPriority(ticket.getPriority());
        response.setPrioritySource(ticket.getPrioritySource());
        response.setCategoryName(ticket.getCategory().getName());
        response.setCreatedByEmail(ticket.getCreatedBy().getEmail());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());
        response.setResolvedAt(ticket.getResolvedAt());

        // assigned agent is nullable — guard against null
        if (ticket.getAssignedAgent() != null) {
            response.setAssignedAgentEmail(ticket.getAssignedAgent().getEmail());
        }

        // SLA overdue computation
        response.setOverdue(computeIsOverdue(ticket));

        return response;
    }

    public TicketSummaryResponse toSummaryResponse(Ticket ticket) {
        TicketSummaryResponse response = new TicketSummaryResponse();
        response.setId(ticket.getId());
        response.setSubject(ticket.getSubject());
        response.setStatus(ticket.getStatus());
        response.setPriority(ticket.getPriority());
        response.setCategoryName(ticket.getCategory().getName());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setOverdue(computeIsOverdue(ticket));
        return response;
    }

    private boolean computeIsOverdue(Ticket ticket) {
        // already resolved/closed — SLA no longer relevant
        if (ticket.getStatus() == Status.RESOLVED || ticket.getStatus() == Status.CLOSED) {
            return false;
        }

        // get SLA window from category (in hours)
        int slaHours = ticket.getCategory().getDefaultSlaHours();

        LocalDateTime slaDeadline = ticket.getCreatedAt().plusHours(slaHours);

        return LocalDateTime.now().isAfter(slaDeadline);
    }
}