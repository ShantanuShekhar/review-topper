package com.reviewtopper.mapper;

import com.reviewtopper.dto.whatsapp.WhatsAppTemplateResponse;
import com.reviewtopper.entity.WhatsAppMessageTemplate;
import org.springframework.stereotype.Component;

@Component
public class WhatsAppTemplateMapper {

    public WhatsAppTemplateResponse toResponse(WhatsAppMessageTemplate t) {
        return new WhatsAppTemplateResponse(
                t.getId(),
                t.getTemplateKey(),
                t.getWorkspace() != null ? t.getWorkspace().getId() : null,
                t.getMessageTemplate(),
                t.isActive());
    }
}
