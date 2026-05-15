package com.reviewtopper.service;

import com.reviewtopper.entity.CustomerInteraction;
import com.reviewtopper.entity.Workspace;
import com.reviewtopper.enums.InteractionSourceType;
import com.reviewtopper.enums.InteractionStatus;
import com.reviewtopper.repository.CustomerInteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final CustomerInteractionRepository interactionRepository;

    @Transactional
    public void record(Workspace workspace, InteractionSourceType sourceType, InteractionStatus status, Map<String, Object> metadata) {
        boolean submitted = status == InteractionStatus.FEEDBACK_SUBMITTED;
        CustomerInteraction row = CustomerInteraction.builder()
                .workspace(workspace)
                .sourceType(sourceType)
                .status(status)
                .submitted(submitted)
                .metadataJson(metadata)
                .build();
        interactionRepository.save(row);
    }
}
