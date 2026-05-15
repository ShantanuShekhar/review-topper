package com.reviewtopper.repository;

import com.reviewtopper.entity.Feedback;
import com.reviewtopper.enums.Sentiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    boolean existsByWorkspaceIdAndVisitorSubmissionKey(Long workspaceId, String visitorSubmissionKey);

    long countByWorkspaceIdAndSentiment(Long workspaceId, Sentiment sentiment);

    long countByWorkspaceId(Long workspaceId);

    @Query("""
            SELECT COUNT(f) FROM Feedback f
            WHERE f.workspace.id = :workspaceId AND f.sentiment = :sentiment
              AND f.createdAt >= :start AND f.createdAt < :end
            """)
    long countSentimentInRange(
            @Param("workspaceId") Long workspaceId,
            @Param("sentiment") Sentiment sentiment,
            @Param("start") Instant start,
            @Param("end") Instant end);

    @Query(value = """
            SELECT DATE(created_at) AS day,
                   SUM(CASE WHEN sentiment = 'POSITIVE' THEN 1 ELSE 0 END) AS happy,
                   SUM(CASE WHEN sentiment = 'NEUTRAL' THEN 1 ELSE 0 END) AS neutral,
                   SUM(CASE WHEN sentiment = 'NEGATIVE' THEN 1 ELSE 0 END) AS bad
            FROM feedbacks
            WHERE workspace_id = :workspaceId
              AND created_at >= :start
              AND created_at < :end
            GROUP BY DATE(created_at)
            ORDER BY day
            """, nativeQuery = true)
    List<DailyFeedbackAggregation> aggregateDailyBySentiment(
            @Param("workspaceId") Long workspaceId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    interface DailyFeedbackAggregation {
        java.sql.Date getDay();

        Number getHappy();

        Number getNeutral();

        Number getBad();
    }
}
