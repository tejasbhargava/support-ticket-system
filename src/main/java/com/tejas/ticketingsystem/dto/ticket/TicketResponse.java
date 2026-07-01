package com.tejas.ticketingsystem.dto.ticket;

import com.tejas.ticketingsystem.enums.Priority;
import com.tejas.ticketingsystem.enums.PrioritySource;
import com.tejas.ticketingsystem.enums.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketResponse {

    private Long id;
    private String subject;
    private String description;
    private Status status;
    private Priority priority;
    private PrioritySource prioritySource;
    private String categoryName;
    private String createdByEmail;
    private String assignedAgentEmail;
    private boolean isOverdue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}