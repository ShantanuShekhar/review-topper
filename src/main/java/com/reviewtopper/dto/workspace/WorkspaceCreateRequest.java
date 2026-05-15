package com.reviewtopper.dto.workspace;

import com.reviewtopper.enums.BusinessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record WorkspaceCreateRequest(
        @Size(max = 160)
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$|^$", message = "Slug must be lowercase alphanumeric with hyphens")
        String slug,
        @NotBlank @Size(max = 255) String name,
        @NotNull BusinessType businessType,
        @Size(max = 2048) String googleReviewLink,
        @Size(max = 64) String phone
) {}
