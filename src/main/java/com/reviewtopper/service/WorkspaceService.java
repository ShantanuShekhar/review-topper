package com.reviewtopper.service;

import com.reviewtopper.dto.workspace.LogoMetadataRequest;
import com.reviewtopper.dto.workspace.ThemeUpdateRequest;
import com.reviewtopper.dto.workspace.WorkspaceCreateRequest;
import com.reviewtopper.dto.workspace.WorkspacePublicResponse;
import com.reviewtopper.dto.workspace.WorkspaceResponse;
import com.reviewtopper.dto.workspace.WorkspaceUpdateRequest;
import com.reviewtopper.entity.ThemeConfiguration;
import com.reviewtopper.entity.User;
import com.reviewtopper.entity.Workspace;
import com.reviewtopper.enums.InteractionSourceType;
import com.reviewtopper.enums.InteractionStatus;
import com.reviewtopper.enums.UserRole;
import com.reviewtopper.exception.BadRequestException;
import com.reviewtopper.exception.ConflictException;
import com.reviewtopper.exception.ForbiddenException;
import com.reviewtopper.exception.NotFoundException;
import com.reviewtopper.mapper.WorkspaceMapper;
import com.reviewtopper.repository.UserRepository;
import com.reviewtopper.repository.WorkspaceRepository;
import com.reviewtopper.security.SecurityUserPrincipal;
import com.reviewtopper.util.SlugGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    public record WorkspaceQrArtifact(byte[] png, String slug) {}

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMapper workspaceMapper;
    private final SubscriptionService subscriptionService;
    private final InteractionService interactionService;
    private final DynamicLabelService dynamicLabelService;
    private final WhatsAppLinkService whatsAppLinkService;
    private final QrCodeService qrCodeService;

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> listFor(SecurityUserPrincipal principal) {
        if (principal.getRole() == UserRole.ADMIN) {
            return workspaceRepository.findAllByActiveTrueOrderByCreatedAtDesc().stream()
                    .map(workspaceMapper::toResponse)
                    .toList();
        }
        return workspaceRepository.findByOwnerIdAndActiveTrueOrderByCreatedAtDesc(principal.getId()).stream()
                .map(workspaceMapper::toResponse)
                .toList();
    }

    @Transactional
    public WorkspaceResponse create(SecurityUserPrincipal principal, WorkspaceCreateRequest req) {
        User owner = userRepository.findById(principal.getId()).orElseThrow(() -> new NotFoundException("User not found"));
        if (principal.getRole() != UserRole.ADMIN) {
            subscriptionService.assertMayCreateWorkspace(owner);
        }

        String slugBase = Optional.ofNullable(req.slug())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElseGet(() -> SlugGenerator.fromName(req.name()));
        if (slugBase.isBlank()) {
            throw new BadRequestException("Could not derive slug; provide slug explicitly.");
        }
        String slug = uniquifySlug(slugBase);

        Workspace workspace = Workspace.builder()
                .slug(slug)
                .name(req.name())
                .businessType(req.businessType())
                .googleReviewLink(req.googleReviewLink())
                .phone(req.phone())
                .owner(owner)
                .buttonStyle(null)
                .active(true)
                .build();
        workspaceRepository.save(workspace);
        byte[] qr = qrCodeService.generateLandingPageQrPng(workspace, 640);
        workspace.setQrCodeBase64(Base64.getEncoder().encodeToString(qr));
        workspaceRepository.save(workspace);
        return workspaceMapper.toResponse(workspace);
    }

    @Transactional
    public WorkspaceResponse update(SecurityUserPrincipal principal, Long workspaceId, WorkspaceUpdateRequest req) {
        Workspace w = requireAccessibleWorkspace(principal, workspaceId);
        String slugBefore = w.getSlug();

        Optional.ofNullable(req.slug())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .ifPresent(newSlug -> {
                    if (!newSlug.equalsIgnoreCase(w.getSlug())
                            && workspaceRepository.existsBySlugIgnoreCaseAndIdNotAndActiveTrue(newSlug, w.getId())) {
                        throw new ConflictException("Slug already in use.");
                    }
                    w.setSlug(newSlug);
                });

        Optional.ofNullable(req.name()).map(String::trim).filter(s -> !s.isEmpty()).ifPresent(w::setName);
        Optional.ofNullable(req.businessType()).ifPresent(w::setBusinessType);
        Optional.ofNullable(req.googleReviewLink()).ifPresent(w::setGoogleReviewLink);
        Optional.ofNullable(req.phone()).ifPresent(w::setPhone);

        workspaceRepository.save(w);
        if (!w.getSlug().equalsIgnoreCase(slugBefore)) {
            refreshStoredQr(w);
        }
        return workspaceMapper.toResponse(w);
    }

    @Transactional
    public WorkspaceResponse updateLogo(SecurityUserPrincipal principal, Long workspaceId, LogoMetadataRequest req) {
        Workspace w = requireAccessibleWorkspace(principal, workspaceId);
        w.setLogoUrl(req.logoUrl());
        workspaceRepository.save(w);
        return workspaceMapper.toResponse(w);
    }

    @Transactional
    public WorkspaceResponse updateTheme(SecurityUserPrincipal principal, Long workspaceId, ThemeUpdateRequest req) {
        Workspace w = requireAccessibleWorkspace(principal, workspaceId);
        ThemeConfiguration tc = w.getThemeConfig();
        if (tc == null) {
            tc = ThemeConfiguration.builder().build();
            w.setThemeConfig(tc);
        }
        String oldPrimary = tc.getPrimaryColor();
        String oldAccent = tc.getAccentColor();
        Boolean oldDark = tc.getDarkModeEnabled();
        Optional.ofNullable(req.primaryColor()).ifPresent(tc::setPrimaryColor);
        Optional.ofNullable(req.secondaryColor()).ifPresent(tc::setSecondaryColor);
        Optional.ofNullable(req.darkModeEnabled()).ifPresent(tc::setDarkModeEnabled);
        Optional.ofNullable(req.logoPosition()).ifPresent(tc::setLogoPosition);
        Optional.ofNullable(req.accentColor()).ifPresent(tc::setAccentColor);
        Optional.ofNullable(req.buttonStyle()).ifPresent(w::setButtonStyle);
        workspaceRepository.save(w);
        ThemeConfiguration after = w.getThemeConfig();
        if (!Objects.equals(oldPrimary, after.getPrimaryColor())
                || !Objects.equals(oldAccent, after.getAccentColor())
                || !Objects.equals(oldDark, after.getDarkModeEnabled())) {
            refreshStoredQr(w);
        }
        return workspaceMapper.toResponse(w);
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getFor(SecurityUserPrincipal principal, Long workspaceId) {
        return workspaceMapper.toResponse(requireAccessibleWorkspace(principal, workspaceId));
    }

    @Transactional
    public void softDelete(SecurityUserPrincipal principal, Long workspaceId) {
        Workspace workspace = requireAccessibleWorkspace(principal, workspaceId);
        workspace.setActive(false);
        workspaceRepository.save(workspace);
    }

    @Transactional
    public WorkspacePublicResponse trackLandingPageAndComposePublicPayload(String slug, String locale) {
        Workspace w = workspaceRepository.findBySlugIgnoreCaseAndActiveTrue(slug)
                .orElseThrow(() -> new NotFoundException("Workspace not found"));

        interactionService.record(
                w,
                InteractionSourceType.LANDING_PAGE,
                InteractionStatus.VISIT,
                Map.of("locale", locale));

        Map<String, String> labels = dynamicLabelService.mergedMap(w.getId(), locale);
        String waLink = whatsAppLinkService.buildWaMeDeepLink(w);

        return workspaceMapper.toPublicResponse(w, waLink, labels);
    }

    @Transactional
    public String redirectToReviewClick(String slug) {
        Workspace w = workspaceRepository.findBySlugIgnoreCaseAndActiveTrue(slug)
                .orElseThrow(() -> new NotFoundException("Workspace not found"));
        if (w.getGoogleReviewLink() == null || w.getGoogleReviewLink().isBlank()) {
            throw new BadRequestException("Review destination URL is not configured for this workspace.");
        }
        interactionService.record(
                w,
                InteractionSourceType.REVIEW_REDIRECT,
                InteractionStatus.REDIRECT_INITIATED,
                Map.of());
        return w.getGoogleReviewLink();
    }

    public Workspace requireAccessibleWorkspace(SecurityUserPrincipal principal, Long workspaceId) {
        Workspace w = workspaceRepository.findByIdAndActiveTrue(workspaceId)
                .orElseThrow(() -> new NotFoundException("Workspace not found"));
        if (principal.getRole() == UserRole.ADMIN) {
            return w;
        }
        if (!w.getOwner().getId().equals(principal.getId())) {
            throw new ForbiddenException("You do not manage this workspace.");
        }
        return w;
    }

    @Transactional(readOnly = true)
    public WorkspaceQrArtifact workspaceQrPng(SecurityUserPrincipal principal, Long workspaceId, int requestedSize) {
        Workspace w = requireAccessibleWorkspace(principal, workspaceId);
        int bounded = Math.min(Math.max(requestedSize, 128), 2048);
        String stored = w.getQrCodeBase64();
        if (bounded == 640 && stored != null && !stored.isBlank()) {
            try {
                return new WorkspaceQrArtifact(Base64.getDecoder().decode(stored), w.getSlug());
            } catch (IllegalArgumentException ignored) {
                // fall through
            }
        }
        byte[] png = qrCodeService.generateLandingPageQrPng(w, bounded);
        return new WorkspaceQrArtifact(png, w.getSlug());
    }

    private void refreshStoredQr(Workspace w) {
        byte[] qr = qrCodeService.generateLandingPageQrPng(w, 640);
        w.setQrCodeBase64(Base64.getEncoder().encodeToString(qr));
        workspaceRepository.save(w);
    }

    @Transactional(readOnly = true)
    public String workspaceWhatsAppDeepLink(SecurityUserPrincipal principal, Long workspaceId) {
        Workspace w = requireAccessibleWorkspace(principal, workspaceId);
        return whatsAppLinkService.buildWaMeDeepLink(w);
    }

    private String uniquifySlug(String slugBase) {
        String candidate = slugBase;
        int counter = 0;
        while (workspaceRepository.existsBySlugIgnoreCaseAndActiveTrue(candidate)) {
            counter++;
            candidate = slugBase + "-" + counter;
            if (candidate.length() > 160) {
                throw new ConflictException("Unable to allocate unique slug; try a different base slug.");
            }
        }
        return candidate;
    }
}
