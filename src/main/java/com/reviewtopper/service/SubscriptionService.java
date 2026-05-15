package com.reviewtopper.service;

import com.reviewtopper.dto.subscription.SubscriptionMeResponse;
import com.reviewtopper.entity.Subscription;
import com.reviewtopper.entity.SubscriptionPlan;
import com.reviewtopper.entity.User;
import com.reviewtopper.exception.BadRequestException;
import com.reviewtopper.exception.BusinessException;
import com.reviewtopper.repository.SubscriptionPlanRepository;
import com.reviewtopper.repository.SubscriptionRepository;
import com.reviewtopper.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final String STARTER_PLAN_NAME = "Starter";

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final WorkspaceRepository workspaceRepository;

    @Transactional
    public void assignStarterSubscription(User user) {
        SubscriptionPlan starter = subscriptionPlanRepository.findByNameIgnoreCase(STARTER_PLAN_NAME)
                .orElseThrow(() -> new IllegalStateException(
                        "Starter subscription plan missing; bootstrap must initialize catalog."));
        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(starter)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(50))
                .active(true)
                .build();
        subscriptionRepository.save(subscription);
    }

    @Transactional(readOnly = true)
    public SubscriptionPlan resolveEffectivePlan(User user) {
        return subscriptionRepository.findPrimaryEffective(user.getId(), LocalDate.now())
                .map(Subscription::getPlan)
                .orElseThrow(() -> new BadRequestException("No active subscription available for this account."));
    }

    @Transactional(readOnly = true)
    public void assertMayCreateWorkspace(User user) {
        SubscriptionPlan plan = resolveEffectivePlan(user);
        long owned = workspaceRepository.countByOwnerIdAndActiveTrue(user.getId());
        if (owned >= plan.getMaxWorkspaces()) {
            throw new BusinessException(
                    "Workspace limit reached for your current plan. Please upgrade to continue.");
        }
    }

    @Transactional(readOnly = true)
    public SubscriptionMeResponse getMySubscriptionSummary(User user) {
        LocalDate today = LocalDate.now();
        Subscription sub = subscriptionRepository
                .findPrimaryEffective(user.getId(), today)
                .orElseThrow(() -> new BadRequestException("No active subscription available for this account."));
        SubscriptionPlan plan = sub.getPlan();
        long used = workspaceRepository.countByOwnerIdAndActiveTrue(user.getId());
        String planName = buildPlanSku(plan);
        String expiryDate = sub.getEndDate().toString();
        return new SubscriptionMeResponse(planName, plan.getMaxWorkspaces(), (int) Math.min(used, Integer.MAX_VALUE), expiryDate);
    }

    private static String buildPlanSku(SubscriptionPlan plan) {
        String slug = plan.getName().trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
        slug = slug.replaceAll("_+", "_").replaceAll("^_|_$", "");
        if (slug.isEmpty()) {
            slug = "PLAN";
        }
        return slug + "_" + plan.getMaxWorkspaces();
    }
}
