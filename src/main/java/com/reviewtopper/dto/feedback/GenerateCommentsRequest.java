package com.reviewtopper.dto.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GenerateCommentsRequest(
        @NotBlank
        @Pattern(regexp = "positive|neutral|negative", flags = jakarta.validation.constraints.Pattern.Flag.CASE_INSENSITIVE)
        String mood,
        @NotBlank
        @Pattern(regexp = "english|hinglish|hindi", flags = jakarta.validation.constraints.Pattern.Flag.CASE_INSENSITIVE)
        String language,
        @Size(max = 160) String workspaceSlug
) {}
