package com.tejas.ticketingsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tejas.ticketingsystem.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long>{
    List<Comment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
