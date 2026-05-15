package com.reviewtopper.dto.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SubmitFeedbackRequest(
        @NotBlank @Size(max = 160) String workspaceSlug,
        @NotBlank
        @Pattern(regexp = "positive|neutral|negative", flags = jakarta.validation.constraints.Pattern.Flag.CASE_INSENSITIVE)
        String mood,
        @NotBlank @Size(max = 8000) String comment,
        @NotBlank
        @Pattern(regexp = "english|hinglish|hindi", flags = jakarta.validation.constraints.Pattern.Flag.CASE_INSENSITIVE)
        String language,
        @NotBlank @Size(min = 8, max = 64) String visitorSubmissionKey
) {}
