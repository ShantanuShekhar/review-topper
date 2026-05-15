package com.reviewtopper.dto.plan;

import com.reviewtopper.enums.DurationType;

import java.math.BigDecimal;

/** Catalog rows exposed on public marketing endpoints — no internal JSON blobs. */
public record PublicPlanResponse(
        Long id,
        String name,
        DurationType durationType,
        int maxWorkspaces,
        BigDecimal price,
        boolean active
) {}
