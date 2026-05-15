package com.reviewtopper.dto.auth;

import com.reviewtopper.enums.UserRole;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        String phone,
        UserRole role,
        boolean verified
) {}
