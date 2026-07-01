package com.tejas.ticketingsystem.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tejas.ticketingsystem.entity.Ticket;
import com.tejas.ticketingsystem.entity.User;
import com.tejas.ticketingsystem.enums.Priority;
import com.tejas.ticketingsystem.enums.Status;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Page<Ticket> findByCreatedBy(User createdBy, Pageable pageable);

    Page<Ticket> findByAssignedAgentOrAssignedAgentIsNull(User agent, Pageable pageable);

    List<Ticket> findByStatusNotIn(List<Status> statuses);

    // count by status for a specific user (customer dashboard)
    long countByCreatedByAndStatus(User createdBy, Status status);

    // count by assigned agent and status (agent dashboard)
    long countByAssignedAgentAndStatus(User assignedAgent, Status status);

    // count by assigned agent and priority (agent dashboard)
    long countByAssignedAgentAndPriority(User assignedAgent, Priority priority);

    // tickets resolved today (admin dashboard)
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.resolvedAt >= :startOfDay")
    long countResolvedToday(@Param("startOfDay") LocalDateTime startOfDay);

    // average resolution time in hours (admin dashboard)
   @Query(value = """
        SELECT AVG(EXTRACT(EPOCH FROM (resolved_at - created_at)) / 3600)
        FROM tickets
        WHERE resolved_at IS NOT NULL
        """, nativeQuery = true)
    Double avgResolutionTimeHours();

    // count tickets grouped by category name (admin dashboard)
    @Query("SELECT t.category.name, COUNT(t) FROM Ticket t GROUP BY t.category.name")
    List<Object[]> countByCategory();

    // count tickets grouped by status (admin dashboard)
    @Query("SELECT t.status, COUNT(t) FROM Ticket t GROUP BY t.status")
    List<Object[]> countGroupedByStatus();

    long countByStatus(Status status);

    long countByPriority(Priority priority);
}
