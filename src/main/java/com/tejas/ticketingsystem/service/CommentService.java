package com.tejas.ticketingsystem.service;

import com.tejas.ticketingsystem.dto.comment.CommentRequest;
import com.tejas.ticketingsystem.dto.comment.CommentResponse;
import com.tejas.ticketingsystem.entity.Comment;
import com.tejas.ticketingsystem.entity.Ticket;
import com.tejas.ticketingsystem.entity.TicketActivity;
import com.tejas.ticketingsystem.entity.User;
import com.tejas.ticketingsystem.enums.ActivityType;
import com.tejas.ticketingsystem.enums.Role;
import com.tejas.ticketingsystem.repository.CommentRepository;
import com.tejas.ticketingsystem.repository.TicketActivityRepository;
import com.tejas.ticketingsystem.repository.TicketRepository;
import com.tejas.ticketingsystem.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final TicketActivityRepository activityRepository;

    public CommentService(
            CommentRepository commentRepository,
            TicketRepository ticketRepository,
            TicketActivityRepository activityRepository
    ) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.activityRepository = activityRepository;
    }

    public CommentResponse addComment(Long ticketId, CommentRequest request) {
        User currentUser = getCurrentUser();

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // customers cannot post internal notes
        boolean isInternal = request.isInternal();
        if (currentUser.getRole() == Role.CUSTOMER && isInternal) {
            throw new RuntimeException("Customers cannot post internal notes");
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setIsInternal(isInternal);
        comment.setTicket(ticket);
        comment.setUser(currentUser);

        Comment saved = commentRepository.save(comment);

        // log activity
        TicketActivity activity = new TicketActivity();
        activity.setTicket(ticket);
        activity.setActivityType(ActivityType.COMMENT_ADDED);
        activity.setDescription("Comment added by " + currentUser.getEmail());
        activity.setPerformedBy(currentUser);
        activityRepository.save(activity);

        return toResponse(saved);
    }

    public List<CommentResponse> getComments(Long ticketId) {
        User currentUser = getCurrentUser();

        ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        List<Comment> comments = commentRepository
                .findByTicketIdOrderByCreatedAtAsc(ticketId);

        return comments.stream()
                .filter(c -> {
                    // customers only see non-internal comments
                    if (currentUser.getRole() == Role.CUSTOMER) {
                        return !c.getIsInternal();
                    }
                    return true; // agents and admins see everything
                })
                .map(this::toResponse)
                .toList();
    }

    private CommentResponse toResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setInternal(comment.getIsInternal());
        response.setAuthorEmail(comment.getUser().getEmail());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }

    private User getCurrentUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return userDetails.getUser();
    }
}