package com.reviewtopper.dto.analytics;

import java.math.BigDecimal;
import java.util.List;

public record AnalyticsResponse(
        long visits,
        long happy,
        long neutral,
        long bad,
        BigDecimal conversionRatePercent,
        List<DailyTrendPoint> dailyTrend
) {
    public record DailyTrendPoint(
            String date,
            long visits,
            long redirects,
            long feedbackVolume,
            long happy,
            long neutral,
            long bad
    ) {}
}
