package com.reviewtopper.dto.subscription;

public record SubscriptionMeResponse(String planName, int maxWorkspaces, int used, String expiryDate) {}
