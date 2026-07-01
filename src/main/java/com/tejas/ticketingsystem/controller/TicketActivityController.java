package com.tejas.ticketingsystem.controller;

import com.tejas.ticketingsystem.entity.TicketActivity;
import com.tejas.ticketingsystem.service.TicketActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/activity")
public class TicketActivityController {

    private final TicketActivityService activityService;

    
    public TicketActivityController(TicketActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<List<TicketActivity>> getActivity(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(activityService.getActivityForTicket(ticketId));
    }
}