package com.reviewtopper.controller;

import com.reviewtopper.dto.plan.PublicPlanResponse;
import com.reviewtopper.service.SubscriptionCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PublicMarketingPlansController {

    private final SubscriptionCatalogService subscriptionCatalogService;

    @GetMapping
    public List<PublicPlanResponse> activePlans() {
        return subscriptionCatalogService.listActivePlansForMarketing();
    }
}
