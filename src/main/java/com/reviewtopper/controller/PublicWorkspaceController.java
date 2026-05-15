package com.reviewtopper.controller;

import com.reviewtopper.dto.workspace.VisitorFeedbackStatusResponse;
import com.reviewtopper.dto.workspace.WorkspacePublicResponse;
import com.reviewtopper.service.FeedbackService;
import com.reviewtopper.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicWorkspaceController {

    private final WorkspaceService workspaceService;
    private final FeedbackService feedbackService;

    @GetMapping("/{workspaceSlug}/visitor-feedback-status")
    public VisitorFeedbackStatusResponse visitorFeedbackStatus(
            @PathVariable String workspaceSlug,
            @RequestParam String visitorSubmissionKey) {
        boolean submitted = feedbackService.hasVisitorSubmittedFeedback(workspaceSlug, visitorSubmissionKey);
        return new VisitorFeedbackStatusResponse(submitted);
    }

    @GetMapping("/{workspaceSlug}")
    public WorkspacePublicResponse publicWorkspace(
            @PathVariable String workspaceSlug,
            @RequestParam(defaultValue = "en") String locale) {
        return workspaceService.trackLandingPageAndComposePublicPayload(workspaceSlug, locale);
    }
}
