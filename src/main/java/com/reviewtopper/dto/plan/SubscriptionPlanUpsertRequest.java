package com.reviewtopper.dto.plan;

import com.reviewtopper.enums.DurationType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SubscriptionPlanUpsertRequest(
        @NotBlank @Size(max = 128) String name,
        @NotNull DurationType durationType,
        @Min(0) int maxWorkspaces,
        @NotNull BigDecimal price,
        @NotBlank String featuresJson,
        Boolean active
) {}
