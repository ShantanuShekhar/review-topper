package com.reviewtopper.controller.admin;

import com.reviewtopper.dto.whatsapp.WhatsAppTemplateResponse;
import com.reviewtopper.dto.whatsapp.WhatsAppTemplateUpsertRequest;
import com.reviewtopper.service.WhatsAppTemplateAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/whatsapp-templates")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminWhatsAppTemplateController {

    private final WhatsAppTemplateAdminService whatsAppTemplateAdminService;

    @GetMapping
    public List<WhatsAppTemplateResponse> list() {
        return whatsAppTemplateAdminService.listAll();
    }

    @PostMapping
    public ResponseEntity<WhatsAppTemplateResponse> create(@Valid @RequestBody WhatsAppTemplateUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(whatsAppTemplateAdminService.create(request));
    }

    @PutMapping("/{templateId}")
    public WhatsAppTemplateResponse replace(@PathVariable Long templateId, @Valid @RequestBody WhatsAppTemplateUpsertRequest request) {
        return whatsAppTemplateAdminService.replace(templateId, request);
    }
}
