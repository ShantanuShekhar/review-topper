package com.reviewtopper.repository;

import com.reviewtopper.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    Optional<SubscriptionPlan> findByNameIgnoreCase(String name);

    List<SubscriptionPlan> findByActiveTrueOrderByPriceAsc();

    List<SubscriptionPlan> findAllByOrderByPriceAsc();
}
