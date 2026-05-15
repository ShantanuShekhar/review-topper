package com.reviewtopper.dto.plan;

import com.reviewtopper.enums.DurationType;

import java.math.BigDecimal;

public record SubscriptionPlanResponse(
        Long id,
        String name,
        DurationType durationType,
        int maxWorkspaces,
        BigDecimal price,
        String featuresJson,
        boolean active
) {}
