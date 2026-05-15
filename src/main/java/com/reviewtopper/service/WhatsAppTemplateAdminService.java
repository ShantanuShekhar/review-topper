package com.reviewtopper.service;

import com.reviewtopper.dto.whatsapp.WhatsAppTemplateResponse;
import com.reviewtopper.dto.whatsapp.WhatsAppTemplateUpsertRequest;
import com.reviewtopper.entity.WhatsAppMessageTemplate;
import com.reviewtopper.exception.BadRequestException;
import com.reviewtopper.exception.NotFoundException;
import com.reviewtopper.mapper.WhatsAppTemplateMapper;
import com.reviewtopper.repository.WhatsAppMessageTemplateRepository;
import com.reviewtopper.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WhatsAppTemplateAdminService {

    private final WhatsAppMessageTemplateRepository whatsAppMessageTemplateRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WhatsAppTemplateMapper whatsAppTemplateMapper;

    @Transactional(readOnly = true)
    public List<WhatsAppTemplateResponse> listAll() {
        return whatsAppMessageTemplateRepository.findAll().stream()
                .map(whatsAppTemplateMapper::toResponse)
                .toList();
    }

    @Transactional
    public WhatsAppTemplateResponse create(WhatsAppTemplateUpsertRequest req) {
        WhatsAppMessageTemplate tpl = WhatsAppMessageTemplate.builder()
                .templateKey(req.templateKey())
                .workspace(resolveWorkspace(req.workspaceId()))
                .messageTemplate(req.messageTemplate())
                .active(req.active() == null ? true : req.active())
                .build();
        whatsAppMessageTemplateRepository.save(tpl);
        return whatsAppTemplateMapper.toResponse(tpl);
    }

    @Transactional
    public WhatsAppTemplateResponse replace(Long id, WhatsAppTemplateUpsertRequest req) {
        WhatsAppMessageTemplate tpl = whatsAppMessageTemplateRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Template not found."));
        tpl.setTemplateKey(req.templateKey());
        tpl.setWorkspace(resolveWorkspace(req.workspaceId()));
        tpl.setMessageTemplate(req.messageTemplate());
        if (req.active() != null) {
            tpl.setActive(req.active());
        }
        whatsAppMessageTemplateRepository.save(tpl);
        return whatsAppTemplateMapper.toResponse(tpl);
    }

    private com.reviewtopper.entity.Workspace resolveWorkspace(Long workspaceId) {
        if (workspaceId == null) {
            return null;
        }
        return workspaceRepository.findByIdAndActiveTrue(workspaceId).orElseThrow(() -> new BadRequestException("Workspace not found."));
    }
}
