package com.reviewtopper.mapper;

import com.reviewtopper.dto.plan.PublicPlanResponse;
import com.reviewtopper.dto.plan.SubscriptionPlanResponse;
import com.reviewtopper.entity.SubscriptionPlan;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionPlanMapper {

    public SubscriptionPlanResponse toResponse(SubscriptionPlan p) {
        return new SubscriptionPlanResponse(
                p.getId(),
                p.getName(),
                p.getDurationType(),
                p.getMaxWorkspaces(),
                p.getPrice(),
                p.getFeaturesJson(),
                p.isActive());
    }

    public PublicPlanResponse toPublicPlan(SubscriptionPlan p) {
        return new PublicPlanResponse(
                p.getId(),
                p.getName(),
                p.getDurationType(),
                p.getMaxWorkspaces(),
                p.getPrice(),
                p.isActive());
    }
}
