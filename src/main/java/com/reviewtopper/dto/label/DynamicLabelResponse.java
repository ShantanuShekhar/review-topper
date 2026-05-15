package com.reviewtopper.dto.label;

public record DynamicLabelResponse(
        Long id,
        Long workspaceId,
        String labelKey,
        String labelValue,
        String category,
        String locale
) {}
