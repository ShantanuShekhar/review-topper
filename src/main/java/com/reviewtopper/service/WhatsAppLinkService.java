package com.reviewtopper.service;

import com.reviewtopper.config.ReviewTopperProperties;
import com.reviewtopper.entity.Workspace;
import com.reviewtopper.exception.BadRequestException;
import com.reviewtopper.repository.WhatsAppMessageTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class WhatsAppLinkService {

    public static final String DEFAULT_TEMPLATE_KEY = "review_invite";

    private final WhatsAppMessageTemplateRepository templateRepository;
    private final ReviewTopperProperties properties;

    @Transactional(readOnly = true)
    public String buildMessageBody(Workspace workspace) {
        var tpl = templateRepository
                .resolveBest(DEFAULT_TEMPLATE_KEY, workspace.getId())
                .orElseThrow(() -> new BadRequestException("WhatsApp template '" + DEFAULT_TEMPLATE_KEY + "' not configured."));
        String reviewLink = workspace.getGoogleReviewLink() != null ? workspace.getGoogleReviewLink() : "";
        String fe = properties.getPublicUrls().getFrontendBaseUrl();
        if (fe == null || fe.isBlank()) {
            fe = "http://localhost:5173";
        }
        String api = properties.getPublicUrls().getApiBaseUrl();
        if (api == null || api.isBlank()) {
            api = "http://localhost:8080";
        }
        String landing = fe.replaceAll("/+$", "") + "/r/" + workspace.getSlug();
        String redirect = api.replaceAll("/+$", "") + "/redirect/" + workspace.getSlug();
        return tpl.getMessageTemplate()
                .replace("{{business_name}}", workspace.getName())
                .replace("{{workspace_slug}}", workspace.getSlug())
                .replace("{{review_link}}", reviewLink)
                .replace("{{public_landing_url}}", landing)
                .replace("{{review_redirect_url}}", redirect);
    }

    public String buildWaMeDeepLink(Workspace workspace) {
        String body = buildMessageBody(workspace);
        String encoded = URLEncoder.encode(body, StandardCharsets.UTF_8);
        return "https://wa.me/?text=" + encoded;
    }
}
