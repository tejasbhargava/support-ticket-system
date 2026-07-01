package com.tejas.ticketingsystem.dto.comment;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentResponse {

    private Long id;
    private String content;
    private boolean isInternal;
    private String authorEmail;
    private LocalDateTime createdAt;
}