package com.reviewtopper.controller;

import com.reviewtopper.dto.plan.SubscriptionPlanResponse;
import com.reviewtopper.service.SubscriptionCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionCatalogService subscriptionCatalogService;

    @GetMapping
    public List<SubscriptionPlanResponse> activePlans() {
        return subscriptionCatalogService.listActivePlansWithFeatures();
    }
}
