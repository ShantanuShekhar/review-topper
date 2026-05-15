package com.reviewtopper.service;

import com.reviewtopper.dto.plan.PublicPlanResponse;
import com.reviewtopper.dto.plan.SubscriptionPlanResponse;
import com.reviewtopper.dto.plan.SubscriptionPlanUpsertRequest;
import com.reviewtopper.entity.SubscriptionPlan;
import com.reviewtopper.exception.NotFoundException;
import com.reviewtopper.mapper.SubscriptionPlanMapper;
import com.reviewtopper.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionCatalogService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    @Transactional(readOnly = true)
    public List<PublicPlanResponse> listActivePlansForMarketing() {
        return subscriptionPlanRepository.findByActiveTrueOrderByPriceAsc().stream()
                .map(subscriptionPlanMapper::toPublicPlan)
                .toList();
    }

    /** Authenticated operators may still resolve full plan rows (includes feature JSON for admin tooling). */
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> listActivePlansWithFeatures() {
        return subscriptionPlanRepository.findByActiveTrueOrderByPriceAsc().stream()
                .map(subscriptionPlanMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> adminListAll() {
        return subscriptionPlanRepository.findAllByOrderByPriceAsc().stream()
                .map(subscriptionPlanMapper::toResponse)
                .toList();
    }

    @Transactional
    public SubscriptionPlanResponse adminCreate(SubscriptionPlanUpsertRequest req) {
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(req.name())
                .durationType(req.durationType())
                .maxWorkspaces(req.maxWorkspaces())
                .price(req.price())
                .featuresJson(req.featuresJson())
                .active(req.active() == null ? true : req.active())
                .build();
        subscriptionPlanRepository.save(plan);
        return subscriptionPlanMapper.toResponse(plan);
    }

    @Transactional
    public SubscriptionPlanResponse adminReplace(Long id, SubscriptionPlanUpsertRequest req) {
        SubscriptionPlan plan =
                subscriptionPlanRepository.findById(id).orElseThrow(() -> new NotFoundException("Plan not found."));
        plan.setName(req.name());
        plan.setDurationType(req.durationType());
        plan.setMaxWorkspaces(req.maxWorkspaces());
        plan.setPrice(req.price());
        plan.setFeaturesJson(req.featuresJson());
        if (req.active() != null) {
            plan.setActive(req.active());
        }
        subscriptionPlanRepository.save(plan);
        return subscriptionPlanMapper.toResponse(plan);
    }

    @Transactional
    public SubscriptionPlanResponse adminSetActive(Long id, boolean active) {
        SubscriptionPlan plan =
                subscriptionPlanRepository.findById(id).orElseThrow(() -> new NotFoundException("Plan not found."));
        plan.setActive(active);
        subscriptionPlanRepository.save(plan);
        return subscriptionPlanMapper.toResponse(plan);
    }
}
