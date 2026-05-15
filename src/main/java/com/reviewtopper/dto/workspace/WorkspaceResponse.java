package com.reviewtopper.dto.workspace;

import com.reviewtopper.entity.ThemeConfiguration;
import com.reviewtopper.enums.BusinessType;
import com.reviewtopper.enums.ButtonStyle;

import java.time.Instant;

public record WorkspaceResponse(
        Long id,
        String slug,
        String name,
        BusinessType businessType,
        String logoUrl,
        String googleReviewLink,
        String phone,
        Long ownerId,
        ThemeConfiguration themeConfig,
        ButtonStyle buttonStyle,
        Instant createdAt,
        Instant updatedAt,
        String reviewPagePath,
        String qrDownloadUrl,
        String qrCodeBase64
) {}
