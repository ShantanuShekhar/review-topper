package com.reviewtopper.controller;

import com.reviewtopper.dto.subscription.SubscriptionMeResponse;
import com.reviewtopper.security.SecurityUtils;
import com.reviewtopper.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionMeController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/me")
    public SubscriptionMeResponse me() {
        return subscriptionService.getMySubscriptionSummary(SecurityUtils.currentUser().getUser());
    }
}
