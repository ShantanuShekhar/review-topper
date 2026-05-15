package com.reviewtopper.repository;

import com.reviewtopper.entity.CustomerInteraction;
import com.reviewtopper.enums.InteractionSourceType;
import com.reviewtopper.enums.InteractionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface CustomerInteractionRepository extends JpaRepository<CustomerInteraction, Long> {

    long countBySourceTypeAndStatus(InteractionSourceType sourceType, InteractionStatus status);

    @Query("""
            SELECT COUNT(c) FROM CustomerInteraction c
            WHERE c.workspace.id = :workspaceId
              AND c.sourceType = :sourceType AND c.status = :status
              AND c.createdAt >= :start AND c.createdAt < :end
            """)
    long countInRange(
            @Param("workspaceId") Long workspaceId,
            @Param("sourceType") InteractionSourceType sourceType,
            @Param("status") InteractionStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end);

    @Query(value = """
            SELECT DATE(created_at) AS day,
                   SUM(CASE WHEN source_type = 'LANDING_PAGE' AND status = 'VISIT' THEN 1 ELSE 0 END) AS visits,
                   SUM(CASE WHEN source_type = 'REVIEW_REDIRECT' AND status = 'REDIRECT_INITIATED' THEN 1 ELSE 0 END) AS redirects
            FROM customer_interactions
            WHERE workspace_id = :workspaceId
              AND created_at >= :start
              AND created_at < :end
            GROUP BY DATE(created_at)
            ORDER BY day
            """, nativeQuery = true)
    List<DailyInteractionAggregation> aggregateDaily(
            @Param("workspaceId") Long workspaceId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    interface DailyInteractionAggregation {
        java.sql.Date getDay();

        Number getVisits();

        Number getRedirects();
    }
}
