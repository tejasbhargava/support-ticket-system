package com.tejas.ticketingsystem.service;

import com.tejas.ticketingsystem.entity.TicketActivity;
import com.tejas.ticketingsystem.repository.TicketActivityRepository;
import com.tejas.ticketingsystem.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketActivityService {

    private final TicketActivityRepository activityRepository;
    private final TicketRepository ticketRepository;

    public TicketActivityService(
            TicketActivityRepository activityRepository,
            TicketRepository ticketRepository
    ) {
        this.activityRepository = activityRepository;
        this.ticketRepository = ticketRepository;
    }

    public List<TicketActivity> getActivityForTicket(Long ticketId) {
        ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        return activityRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }
}