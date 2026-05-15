package com.reviewtopper.mapper;

import com.reviewtopper.dto.workspace.WorkspacePublicResponse;
import com.reviewtopper.dto.workspace.WorkspaceResponse;
import com.reviewtopper.entity.ThemeConfiguration;
import com.reviewtopper.entity.Workspace;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WorkspaceMapper {

    public WorkspaceResponse toResponse(Workspace w) {
        String reviewPagePath = "/r/" + w.getSlug();
        String qrDownloadUrl = "/api/workspaces/" + w.getId() + "/qr/download";
        return new WorkspaceResponse(
                w.getId(),
                w.getSlug(),
                w.getName(),
                w.getBusinessType(),
                w.getLogoUrl(),
                w.getGoogleReviewLink(),
                w.getPhone(),
                w.getOwner().getId(),
                w.getThemeConfig(),
                w.getButtonStyle(),
                w.getCreatedAt(),
                w.getUpdatedAt(),
                reviewPagePath,
                qrDownloadUrl,
                w.getQrCodeBase64());
    }

    public WorkspacePublicResponse toPublicResponse(
            Workspace w,
            String whatsappDeepLink,
            Map<String, String> labels) {
        ThemeConfiguration tc = w.getThemeConfig();
        String primaryColor =
                tc != null && tc.getPrimaryColor() != null ? tc.getPrimaryColor() : "#6366f1";
        String theme =
                tc != null && Boolean.TRUE.equals(tc.getDarkModeEnabled()) ? "dark" : "light";
        return new WorkspacePublicResponse(
                w.getId(),
                w.getSlug(),
                w.getName(),
                w.getBusinessType(),
                w.getLogoUrl(),
                w.getGoogleReviewLink(),
                w.getPhone(),
                tc,
                w.getButtonStyle(),
                primaryColor,
                theme,
                whatsappDeepLink,
                labels);
    }
}
