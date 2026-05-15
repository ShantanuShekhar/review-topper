package com.reviewtopper.dto.feedback;

import com.reviewtopper.enums.FeedbackLanguage;
import com.reviewtopper.enums.Sentiment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FeedbackSubmitRequest(
        @NotBlank @Size(max = 160) String workspaceSlug,
        @NotBlank @Size(max = 8000) String message,
        @NotNull Sentiment sentiment,
        /** Stable per-browser id (e.g. UUID); one private feedback per visitor per workspace. */
        @NotBlank @Size(min = 8, max = 64) String visitorSubmissionKey,
        FeedbackLanguage language
) {}
