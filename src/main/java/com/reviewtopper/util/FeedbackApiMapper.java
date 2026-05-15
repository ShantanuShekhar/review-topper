package com.reviewtopper.util;

import com.reviewtopper.enums.FeedbackLanguage;
import com.reviewtopper.enums.Sentiment;

public final class FeedbackApiMapper {

    private FeedbackApiMapper() {}

    public static Sentiment toSentiment(String mood) {
        return switch (mood.trim().toLowerCase()) {
            case "positive" -> Sentiment.POSITIVE;
            case "negative" -> Sentiment.NEGATIVE;
            default -> Sentiment.NEUTRAL;
        };
    }

    public static FeedbackLanguage toLanguage(String language) {
        return switch (language.trim().toLowerCase()) {
            case "hinglish" -> FeedbackLanguage.HINGLISH;
            case "hindi" -> FeedbackLanguage.HINDI;
            default -> FeedbackLanguage.ENGLISH;
        };
    }
}
