package com.tejas.ticketingsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tejas.ticketingsystem.entity.TicketActivity;

public interface TicketActivityRepository extends JpaRepository<TicketActivity, Long>{
    List<TicketActivity> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
