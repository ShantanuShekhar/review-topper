package com.reviewtopper.repository;

import com.reviewtopper.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserIdOrderByEndDateDesc(Long userId);

    @Query("""
            SELECT s FROM Subscription s
            JOIN FETCH s.plan p
            WHERE s.user.id = :userId AND s.active = true
             AND :today BETWEEN s.startDate AND s.endDate
            ORDER BY s.endDate DESC
            """)
    List<Subscription> findEffectiveSubscriptions(@Param("userId") Long userId, @Param("today") LocalDate today);

    default Optional<Subscription> findPrimaryEffective(Long userId, LocalDate today) {
        List<Subscription> list = findEffectiveSubscriptions(userId, today);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
