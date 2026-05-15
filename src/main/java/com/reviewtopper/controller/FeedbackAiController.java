package com.reviewtopper.controller;

import com.reviewtopper.dto.feedback.GenerateCommentsRequest;
import com.reviewtopper.dto.feedback.GenerateCommentsResponse;
import com.reviewtopper.dto.feedback.SubmitFeedbackRequest;
import com.reviewtopper.dto.feedback.SubmitFeedbackResponse;
import com.reviewtopper.service.CommentGenerationService;
import com.reviewtopper.service.FeedbackService;
import com.reviewtopper.util.FeedbackApiMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FeedbackAiController {

    private final CommentGenerationService commentGenerationService;
    private final FeedbackService feedbackService;

    @PostMapping("/generate-comments")
    public GenerateCommentsResponse generateComments(@Valid @RequestBody GenerateCommentsRequest request) {
        var sentiment = FeedbackApiMapper.toSentiment(request.mood());
        var language = FeedbackApiMapper.toLanguage(request.language());
        var comments = commentGenerationService.generate(sentiment, language, request.workspaceSlug());
        return new GenerateCommentsResponse(comments);
    }

    @PostMapping("/submit-feedback")
    public ResponseEntity<SubmitFeedbackResponse> submitFeedback(@Valid @RequestBody SubmitFeedbackRequest request) {
        SubmitFeedbackResponse body = feedbackService.submitFromAi(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
