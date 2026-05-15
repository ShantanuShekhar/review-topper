package com.reviewtopper.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 255) String name,
        @Email @NotBlank @Size(max = 320) String email,
        @Size(max = 64) String phone,
        @NotBlank @Size(min = 8, max = 128) String password
) {}
