package com.tejas.ticketingsystem.dto.ticket;

import com.tejas.ticketingsystem.enums.Priority;
import com.tejas.ticketingsystem.enums.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketSummaryResponse {

    private Long id;
    private String subject;
    private Status status;
    private Priority priority;
    private String categoryName;
    private boolean isOverdue;
    private LocalDateTime createdAt;
}