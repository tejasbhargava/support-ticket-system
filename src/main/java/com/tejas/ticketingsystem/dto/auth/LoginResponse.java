package com.tejas.ticketingsystem.dto.auth;

import com.tejas.ticketingsystem.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String email;
    private Role role;
}
