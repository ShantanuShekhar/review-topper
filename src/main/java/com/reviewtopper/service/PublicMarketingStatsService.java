package com.reviewtopper.service;

import com.reviewtopper.dto.marketing.PublicMarketingStatsResponse;
import com.reviewtopper.enums.InteractionSourceType;
import com.reviewtopper.enums.InteractionStatus;
import com.reviewtopper.repository.CustomerInteractionRepository;
import com.reviewtopper.repository.FeedbackRepository;
import com.reviewtopper.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublicMarketingStatsService {

    private final WorkspaceRepository workspaceRepository;
    private final FeedbackRepository feedbackRepository;
    private final CustomerInteractionRepository customerInteractionRepository;

    @Transactional(readOnly = true)
    public PublicMarketingStatsResponse snapshot() {
        long workspaces = workspaceRepository.countByActiveTrue();
        long feedback = feedbackRepository.count();
        long redirects = customerInteractionRepository.countBySourceTypeAndStatus(
                InteractionSourceType.REVIEW_REDIRECT, InteractionStatus.REDIRECT_INITIATED);
        return new PublicMarketingStatsResponse(workspaces, feedback, redirects);
    }
}
