package com.reviewtopper.dto.auth;

import com.reviewtopper.enums.UserRole;

public record JwtResponse(String token, String tokenType, Long userId, String email, UserRole role) {

    public JwtResponse(String token, Long userId, String email, UserRole role) {
        this(token, "Bearer", userId, email, role);
    }
}
