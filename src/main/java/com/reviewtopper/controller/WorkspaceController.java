package com.reviewtopper.controller;

import com.reviewtopper.dto.analytics.AnalyticsResponse;
import com.reviewtopper.dto.workspace.LogoMetadataRequest;
import com.reviewtopper.dto.workspace.ThemeUpdateRequest;
import com.reviewtopper.dto.workspace.WorkspaceCreateRequest;
import com.reviewtopper.dto.workspace.WorkspaceResponse;
import com.reviewtopper.dto.workspace.WorkspaceUpdateRequest;
import com.reviewtopper.security.SecurityUtils;
import com.reviewtopper.service.AnalyticsService;
import com.reviewtopper.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final AnalyticsService analyticsService;

    @GetMapping
    public List<WorkspaceResponse> list() {
        return workspaceService.listFor(SecurityUtils.currentUser());
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> create(@Valid @RequestBody WorkspaceCreateRequest request) {
        WorkspaceResponse created = workspaceService.create(SecurityUtils.currentUser(), request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> delete(@PathVariable Long workspaceId) {
        workspaceService.softDelete(SecurityUtils.currentUser(), workspaceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{workspaceId}")
    public WorkspaceResponse one(@PathVariable Long workspaceId) {
        return workspaceService.getFor(SecurityUtils.currentUser(), workspaceId);
    }

    @PutMapping("/{workspaceId}")
    public WorkspaceResponse update(@PathVariable Long workspaceId, @Valid @RequestBody WorkspaceUpdateRequest request) {
        return workspaceService.update(SecurityUtils.currentUser(), workspaceId, request);
    }

    @PostMapping("/{workspaceId}/logo-metadata")
    public WorkspaceResponse logo(@PathVariable Long workspaceId, @Valid @RequestBody LogoMetadataRequest request) {
        return workspaceService.updateLogo(SecurityUtils.currentUser(), workspaceId, request);
    }

    @PatchMapping("/{workspaceId}/theme")
    public WorkspaceResponse theme(@PathVariable Long workspaceId, @Valid @RequestBody ThemeUpdateRequest request) {
        return workspaceService.updateTheme(SecurityUtils.currentUser(), workspaceId, request);
    }

    @GetMapping("/{workspaceId}/analytics")
    public AnalyticsResponse analytics(@PathVariable Long workspaceId, @RequestParam(defaultValue = "30") int days) {
        workspaceService.requireAccessibleWorkspace(SecurityUtils.currentUser(), workspaceId);
        return analyticsService.aggregate(workspaceId, days);
    }

    @GetMapping(value = "/{workspaceId}/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qr(@PathVariable Long workspaceId, @RequestParam(defaultValue = "640") int size) {
        WorkspaceService.WorkspaceQrArtifact artifact =
                workspaceService.workspaceQrPng(SecurityUtils.currentUser(), workspaceId, size);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"qr-%s.png\"".formatted(artifact.slug()))
                .body(artifact.png());
    }

    @GetMapping(value = "/{workspaceId}/qr/download", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrDownload(@PathVariable Long workspaceId, @RequestParam(defaultValue = "640") int size) {
        WorkspaceService.WorkspaceQrArtifact artifact =
                workspaceService.workspaceQrPng(SecurityUtils.currentUser(), workspaceId, size);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"qr-%s.png\"".formatted(artifact.slug()))
                .body(artifact.png());
    }

    @GetMapping("/{workspaceId}/whatsapp-link")
    public Map<String, String> whatsapp(@PathVariable Long workspaceId) {
        return Map.of("deepLink", workspaceService.workspaceWhatsAppDeepLink(SecurityUtils.currentUser(), workspaceId));
    }
}
