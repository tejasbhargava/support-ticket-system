package com.tejas.ticketingsystem.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequest {

    @NotBlank(message = "Content is required")
    private String content;

    private boolean isInternal = false;
}