package com.reviewtopper.service;

import com.reviewtopper.dto.analytics.AnalyticsResponse;
import com.reviewtopper.enums.InteractionSourceType;
import com.reviewtopper.enums.InteractionStatus;
import com.reviewtopper.enums.Sentiment;
import com.reviewtopper.repository.CustomerInteractionRepository;
import com.reviewtopper.repository.CustomerInteractionRepository.DailyInteractionAggregation;
import com.reviewtopper.repository.FeedbackRepository;
import com.reviewtopper.repository.FeedbackRepository.DailyFeedbackAggregation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final CustomerInteractionRepository interactionRepository;
    private final FeedbackRepository feedbackRepository;

    @Transactional(readOnly = true)
    public AnalyticsResponse aggregate(Long workspaceId, int days) {
        int boundedDays = Math.min(Math.max(days, 1), 366);
        Instant end = Instant.now();
        Instant start = end.minus(boundedDays, ChronoUnit.DAYS);

        long visits = interactionRepository.countInRange(
                workspaceId, InteractionSourceType.LANDING_PAGE, InteractionStatus.VISIT, start, end);
        long redirects = interactionRepository.countInRange(
                workspaceId,
                InteractionSourceType.REVIEW_REDIRECT,
                InteractionStatus.REDIRECT_INITIATED,
                start,
                end);

        long happy = feedbackRepository.countSentimentInRange(workspaceId, Sentiment.POSITIVE, start, end);
        long neutral = feedbackRepository.countSentimentInRange(workspaceId, Sentiment.NEUTRAL, start, end);
        long bad = feedbackRepository.countSentimentInRange(workspaceId, Sentiment.NEGATIVE, start, end);

        BigDecimal conversion = visits == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(redirects)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(visits), 2, RoundingMode.HALF_UP);

        List<DailyInteractionAggregation> ia = interactionRepository.aggregateDaily(workspaceId, start, end);
        List<DailyFeedbackAggregation> fa = feedbackRepository.aggregateDailyBySentiment(workspaceId, start, end);

        Map<LocalDate, TrendAcc> trendMap = new HashMap<>();
        LocalDate cursor = LocalDate.ofInstant(start, ZoneOffset.UTC);
        LocalDate endDay = LocalDate.ofInstant(end, ZoneOffset.UTC);
        while (!cursor.isAfter(endDay)) {
            trendMap.put(cursor, new TrendAcc());
            cursor = cursor.plusDays(1);
        }

        for (DailyInteractionAggregation row : ia) {
            LocalDate day = row.getDay().toLocalDate();
            TrendAcc acc = trendMap.computeIfAbsent(day, d -> new TrendAcc());
            acc.visits += nz(row.getVisits());
            acc.redirects += nz(row.getRedirects());
        }

        for (DailyFeedbackAggregation row : fa) {
            LocalDate day = row.getDay().toLocalDate();
            TrendAcc acc = trendMap.computeIfAbsent(day, d -> new TrendAcc());
            acc.happy += nz(row.getHappy());
            acc.neutral += nz(row.getNeutral());
            acc.bad += nz(row.getBad());
        }

        List<AnalyticsResponse.DailyTrendPoint> trend = new ArrayList<>();
        trendMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    TrendAcc a = e.getValue();
                    long feedbackVol = a.happy + a.neutral + a.bad;
                    trend.add(new AnalyticsResponse.DailyTrendPoint(
                            e.getKey().toString(),
                            a.visits,
                            a.redirects,
                            feedbackVol,
                            a.happy,
                            a.neutral,
                            a.bad));
                });

        return new AnalyticsResponse(visits, happy, neutral, bad, conversion, trend);
    }

    private static long nz(Number n) {
        return n == null ? 0L : n.longValue();
    }

    private static final class TrendAcc {
        private long visits;
        private long redirects;
        private long happy;
        private long neutral;
        private long bad;
    }
}
