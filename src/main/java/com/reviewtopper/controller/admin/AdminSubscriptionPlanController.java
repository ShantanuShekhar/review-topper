package com.reviewtopper.controller.admin;

import com.reviewtopper.dto.plan.SubscriptionPlanActivePatchRequest;
import com.reviewtopper.dto.plan.SubscriptionPlanResponse;
import com.reviewtopper.dto.plan.SubscriptionPlanUpsertRequest;
import com.reviewtopper.service.SubscriptionCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/subscription-plans")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSubscriptionPlanController {

    private final SubscriptionCatalogService subscriptionCatalogService;

    @GetMapping
    public List<SubscriptionPlanResponse> all() {
        return subscriptionCatalogService.adminListAll();
    }

    @PostMapping
    public ResponseEntity<SubscriptionPlanResponse> create(@Valid @RequestBody SubscriptionPlanUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionCatalogService.adminCreate(request));
    }

    @PutMapping("/{planId}")
    public SubscriptionPlanResponse replace(@PathVariable Long planId, @Valid @RequestBody SubscriptionPlanUpsertRequest request) {
        return subscriptionCatalogService.adminReplace(planId, request);
    }

    @PatchMapping("/{planId}/active")
    public SubscriptionPlanResponse activation(@PathVariable Long planId, @Valid @RequestBody SubscriptionPlanActivePatchRequest request) {
        return subscriptionCatalogService.adminSetActive(planId, request.active());
    }
}
