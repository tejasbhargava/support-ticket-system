package com.tejas.ticketingsystem.service;

import com.tejas.ticketingsystem.dto.ticket.TicketRequest;
import com.tejas.ticketingsystem.dto.ticket.TicketResponse;
import com.tejas.ticketingsystem.dto.ticket.TicketSummaryResponse;
import com.tejas.ticketingsystem.entity.Category;
import com.tejas.ticketingsystem.entity.Ticket;
import com.tejas.ticketingsystem.entity.TicketActivity;
import com.tejas.ticketingsystem.entity.User;
import com.tejas.ticketingsystem.enums.ActivityType;
import com.tejas.ticketingsystem.enums.Priority;
import com.tejas.ticketingsystem.enums.PrioritySource;
import com.tejas.ticketingsystem.enums.Role;
import com.tejas.ticketingsystem.enums.Status;
import com.tejas.ticketingsystem.mapper.TicketMapper;
import com.tejas.ticketingsystem.repository.CategoryRepository;
import com.tejas.ticketingsystem.repository.TicketActivityRepository;
import com.tejas.ticketingsystem.repository.TicketRepository;
import com.tejas.ticketingsystem.repository.UserRepository;
import com.tejas.ticketingsystem.rules.PriorityEngine;
import com.tejas.ticketingsystem.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TicketActivityRepository activityRepository;
    private final PriorityEngine priorityEngine;
    private final TicketMapper ticketMapper;

    public TicketService(
            TicketRepository ticketRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            TicketActivityRepository activityRepository,
            PriorityEngine priorityEngine,
            TicketMapper ticketMapper
    ) {
        this.ticketRepository = ticketRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.priorityEngine = priorityEngine;
        this.ticketMapper = ticketMapper;
    }

    // ── CREATE ──────────────────────────────────────────────

    public TicketResponse createTicket(TicketRequest request) {
        User currentUser = getCurrentUser();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // run through priority engine
        Priority autoPriority = priorityEngine.determinePriority(category, request.getDescription());

        Ticket ticket = new Ticket();
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(category);
        ticket.setCreatedBy(currentUser);
        ticket.setStatus(Status.OPEN);
        ticket.setPriority(autoPriority);
        ticket.setPrioritySource(PrioritySource.AUTO);

        Ticket saved = ticketRepository.save(ticket);

        // log activity
        logActivity(saved, ActivityType.TICKET_CREATED,
                "Ticket created by " + currentUser.getEmail(), currentUser);

        return ticketMapper.toResponse(saved);
    }

    // ── GET ALL (role-scoped) ───────────────────────────────

    public Page<TicketSummaryResponse> getAllTickets(Pageable pageable) {
        User currentUser = getCurrentUser();

        Page<Ticket> tickets = switch (currentUser.getRole()) {
            case CUSTOMER -> ticketRepository.findByCreatedBy(currentUser, pageable);
            case AGENT -> ticketRepository.findByAssignedAgentOrAssignedAgentIsNull(currentUser, pageable);
            case ADMIN -> ticketRepository.findAll(pageable);
        };

        return tickets.map(ticketMapper::toSummaryResponse);
    }

    // ── GET ONE ─────────────────────────────────────────────

    public TicketResponse getTicketById(Long id) {
        Ticket ticket = findTicketWithAccessCheck(id);
        return ticketMapper.toResponse(ticket);
    }

    // ── STATUS TRANSITION ───────────────────────────────────

    public TicketResponse updateStatus(Long id, Status newStatus) {
        User currentUser = getCurrentUser();
        Ticket ticket = findTicketWithAccessCheck(id);

        validateTransition(ticket.getStatus(), newStatus, currentUser);

        Status oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);

        if (newStatus == Status.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        Ticket saved = ticketRepository.save(ticket);

        logActivity(saved, ActivityType.STATUS_CHANGED,
                "Status changed from " + oldStatus + " to " + newStatus + " by " + currentUser.getEmail(),
                currentUser);

        return ticketMapper.toResponse(saved);
    }

    // ── ASSIGN AGENT ────────────────────────────────────────

    public TicketResponse assignAgent(Long ticketId, Long agentId) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only admins can assign tickets");
        }
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        
        if (agent.getRole() != Role.AGENT) {
            throw new RuntimeException("Selected user is not an agent");
        }

        ticket.setAssignedAgent(agent);
        Ticket saved = ticketRepository.save(ticket);

        logActivity(saved, ActivityType.ASSIGNED,
                "Assigned to " + agent.getEmail() + " by " + currentUser.getEmail(),
                currentUser);

        return ticketMapper.toResponse(saved);
    }

    // ── OVERRIDE PRIORITY ───────────────────────────────────

    public TicketResponse updatePriority(Long id, Priority newPriority) {
        User currentUser = getCurrentUser();
        Ticket ticket = findTicketWithAccessCheck(id);

        ticket.setPriority(newPriority);
        ticket.setPrioritySource(PrioritySource.MANUAL);
        Ticket saved = ticketRepository.save(ticket);

        logActivity(saved, ActivityType.PRIORITY_CHANGED,
                "Priority changed to " + newPriority + " by " + currentUser.getEmail(),
                currentUser);

        return ticketMapper.toResponse(saved);
    }

    // ── OVERDUE TICKETS ─────────────────────────────────────

    public List<TicketSummaryResponse> getOverdueTickets() {
        List<Ticket> allOpen = ticketRepository
                .findByStatusNotIn(List.of(Status.RESOLVED, Status.CLOSED));

        return allOpen.stream()
                .filter(t -> {
                    LocalDateTime deadline = t.getCreatedAt()
                            .plusHours(t.getCategory().getDefaultSlaHours());
                    return LocalDateTime.now().isAfter(deadline);
                })
                .map(ticketMapper::toSummaryResponse)
                .toList();
    }

    // ── HELPERS ─────────────────────────────────────────────

    private User getCurrentUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return userDetails.getUser();
    }

    private Ticket findTicketWithAccessCheck(Long id) {
        User currentUser = getCurrentUser();
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        return switch (currentUser.getRole()) {
            case CUSTOMER -> {
                if (!ticket.getCreatedBy().getId().equals(currentUser.getId())) {
                    throw new RuntimeException("Access denied");
                }
                yield ticket;
            }
            case AGENT -> {
                if (ticket.getAssignedAgent() == null ||
                        !ticket.getAssignedAgent().getId().equals(currentUser.getId())) {
                    throw new RuntimeException("Access denied");
                }
                yield ticket;
            }
            case ADMIN -> ticket;
        };
    }

    private void validateTransition(Status current, Status next, User user) {
        // define valid transitions
        boolean valid = switch (current) {
            case OPEN -> next == Status.IN_PROGRESS;
            case IN_PROGRESS -> next == Status.WAITING_FOR_CUSTOMER || next == Status.RESOLVED;
            case WAITING_FOR_CUSTOMER -> next == Status.IN_PROGRESS || next == Status.RESOLVED;
            case RESOLVED -> next == Status.CLOSED || next == Status.REOPENED;
            case CLOSED -> false; // terminal state
            case REOPENED -> next == Status.IN_PROGRESS;
        };

        if (!valid) {
            throw new RuntimeException(
                    "Cannot transition from " + current + " to " + next);
        }
    }

    private void logActivity(Ticket ticket, ActivityType type, String description, User performedBy) {
        TicketActivity activity = new TicketActivity();
        activity.setTicket(ticket);
        activity.setActivityType(type);
        activity.setDescription(description);
        activity.setPerformedBy(performedBy);
        activityRepository.save(activity);
    }
}