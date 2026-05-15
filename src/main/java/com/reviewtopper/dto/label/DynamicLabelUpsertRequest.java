package com.reviewtopper.dto.label;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DynamicLabelUpsertRequest(
        Long workspaceId,
        @NotBlank @Size(max = 128) String labelKey,
        @NotBlank @Size(max = 1024) String labelValue,
        @Size(max = 64) String category,
        @NotBlank @Size(max = 16) String locale
) {}
