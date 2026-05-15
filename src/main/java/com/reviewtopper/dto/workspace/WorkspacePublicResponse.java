package com.reviewtopper.dto.workspace;

import com.reviewtopper.entity.ThemeConfiguration;
import com.reviewtopper.enums.BusinessType;
import com.reviewtopper.enums.ButtonStyle;

import java.util.Map;

/**
 * Guest-facing workspace payload. Paths like {@code /redirect/:slug} stay server-side — clients use {@code slug}
 * with known routes.
 */
public record WorkspacePublicResponse(
        Long id,
        String slug,
        String name,
        BusinessType businessType,
        String logoUrl,
        String googleReviewLink,
        String phone,
        ThemeConfiguration themeConfig,
        ButtonStyle buttonStyle,
        String primaryColor,
        String theme,
        String whatsappDeepLink,
        Map<String, String> labels
) {}
