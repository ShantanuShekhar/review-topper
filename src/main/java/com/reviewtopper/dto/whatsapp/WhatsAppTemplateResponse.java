package com.reviewtopper.dto.whatsapp;

public record WhatsAppTemplateResponse(
        Long id,
        String templateKey,
        Long workspaceId,
        String messageTemplate,
        boolean active
) {}
