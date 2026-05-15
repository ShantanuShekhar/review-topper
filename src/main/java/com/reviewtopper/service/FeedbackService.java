package com.reviewtopper.service;

import com.reviewtopper.dto.feedback.FeedbackSubmitRequest;
import com.reviewtopper.dto.feedback.SubmitFeedbackRequest;
import com.reviewtopper.dto.feedback.SubmitFeedbackResponse;
import com.reviewtopper.entity.Feedback;
import com.reviewtopper.enums.FeedbackLanguage;
import com.reviewtopper.enums.Sentiment;
import com.reviewtopper.exception.ConflictException;
import com.reviewtopper.exception.NotFoundException;
import com.reviewtopper.enums.InteractionSourceType;
import com.reviewtopper.enums.InteractionStatus;
import com.reviewtopper.util.FeedbackApiMapper;
import com.reviewtopper.repository.FeedbackRepository;
import com.reviewtopper.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final WorkspaceRepository workspaceRepository;
    private final FeedbackRepository feedbackRepository;
    private final InteractionService interactionService;

    @Transactional(readOnly = true)
    public boolean hasVisitorSubmittedFeedback(String workspaceSlug, String visitorSubmissionKey) {
        if (visitorSubmissionKey == null
                || visitorSubmissionKey.isBlank()
                || visitorSubmissionKey.length() < 8
                || visitorSubmissionKey.length() > 64) {
            return false;
        }
        return workspaceRepository
                .findBySlugIgnoreCaseAndActiveTrue(workspaceSlug)
                .map(w -> feedbackRepository.existsByWorkspaceIdAndVisitorSubmissionKey(w.getId(), visitorSubmissionKey))
                .orElse(false);
    }

    @Transactional
    public Long submit(FeedbackSubmitRequest request) {
        FeedbackLanguage language = request.language() != null ? request.language() : FeedbackLanguage.ENGLISH;
        return submitInternal(
                request.workspaceSlug(),
                request.message(),
                request.sentiment(),
                language,
                request.visitorSubmissionKey());
    }

    @Transactional
    public SubmitFeedbackResponse submitFromAi(SubmitFeedbackRequest request) {
        Sentiment sentiment = FeedbackApiMapper.toSentiment(request.mood());
        FeedbackLanguage language = FeedbackApiMapper.toLanguage(request.language());
        Long id = submitInternal(
                request.workspaceSlug(),
                request.comment(),
                sentiment,
                language,
                request.visitorSubmissionKey());
        return new SubmitFeedbackResponse(id, sentiment == Sentiment.POSITIVE);
    }

    private Long submitInternal(
            String workspaceSlug,
            String comment,
            Sentiment sentiment,
            FeedbackLanguage language,
            String visitorSubmissionKey) {
        var workspace = workspaceRepository
                .findBySlugIgnoreCaseAndActiveTrue(workspaceSlug)
                .orElseThrow(() -> new NotFoundException("Workspace not found"));
        if (feedbackRepository.existsByWorkspaceIdAndVisitorSubmissionKey(workspace.getId(), visitorSubmissionKey)) {
            throw new ConflictException("You've already submitted feedback. Thank you!");
        }
        Feedback fb = Feedback.builder()
                .workspace(workspace)
                .message(comment)
                .sentiment(sentiment)
                .language(language)
                .visitorSubmissionKey(visitorSubmissionKey)
                .build();
        try {
            feedbackRepository.save(fb);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("You've already submitted feedback. Thank you!");
        }
        interactionService.record(
                workspace,
                InteractionSourceType.FEEDBACK_FORM,
                InteractionStatus.FEEDBACK_SUBMITTED,
                Map.of(
                        "feedbackId",
                        fb.getId(),
                        "sentiment",
                        sentiment.name(),
                        "language",
                        language.name(),
                        "visitorSubmissionKey",
                        visitorSubmissionKey));
        return fb.getId();
    }
}
