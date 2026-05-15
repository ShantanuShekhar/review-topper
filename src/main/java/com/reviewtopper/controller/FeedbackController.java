package com.reviewtopper.controller;

import com.reviewtopper.dto.feedback.FeedbackSubmitRequest;
import com.reviewtopper.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<Map<String, Long>> submit(@Valid @RequestBody FeedbackSubmitRequest request) {
        Long id = feedbackService.submit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }
}
