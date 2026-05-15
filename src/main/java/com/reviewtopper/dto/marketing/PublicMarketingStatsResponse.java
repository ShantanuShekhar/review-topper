package com.reviewtopper.dto.marketing;

public record PublicMarketingStatsResponse(
        long workspaceCount,
        long feedbackCount,
        long reviewRedirectCount
) {}
