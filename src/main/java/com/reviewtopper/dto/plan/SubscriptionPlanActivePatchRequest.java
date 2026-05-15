package com.reviewtopper.dto.plan;

import jakarta.validation.constraints.NotNull;

public record SubscriptionPlanActivePatchRequest(@NotNull Boolean active) {}
