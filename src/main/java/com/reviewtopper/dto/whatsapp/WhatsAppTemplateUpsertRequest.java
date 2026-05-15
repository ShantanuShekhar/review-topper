package com.reviewtopper.dto.whatsapp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WhatsAppTemplateUpsertRequest(
        @NotBlank @Size(max = 64) String templateKey,
        Long workspaceId,
        @NotBlank String messageTemplate,
        Boolean active
) {}
