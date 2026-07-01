package com.tejas.ticketingsystem.controller;

import com.tejas.ticketingsystem.dto.ticket.TicketRequest;
import com.tejas.ticketingsystem.dto.ticket.TicketResponse;
import com.tejas.ticketingsystem.dto.ticket.TicketSummaryResponse;
import com.tejas.ticketingsystem.enums.Priority;
import com.tejas.ticketingsystem.enums.Status;
import com.tejas.ticketingsystem.service.TicketService;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springdoc.core.annotations.ParameterObject;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody TicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.createTicket(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'ADMIN')")
    public ResponseEntity<Page<TicketSummaryResponse>> getAllTickets(
            @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(ticketService.getAllTickets(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'ADMIN')")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam Status newStatus) {
        return ResponseEntity.ok(ticketService.updateStatus(id, newStatus));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> assignAgent(
            @PathVariable Long id,
            @RequestParam Long agentId) {
        return ResponseEntity.ok(ticketService.assignAgent(id, agentId));
    }

    @PatchMapping("/{id}/priority")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> updatePriority(
            @PathVariable Long id,
            @RequestParam Priority newPriority) {
        return ResponseEntity.ok(ticketService.updatePriority(id, newPriority));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<List<TicketSummaryResponse>> getOverdueTickets() {
        return ResponseEntity.ok(ticketService.getOverdueTickets());
    }
}