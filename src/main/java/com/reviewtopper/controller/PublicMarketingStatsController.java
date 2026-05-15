package com.reviewtopper.controller;

import com.reviewtopper.dto.marketing.PublicMarketingStatsResponse;
import com.reviewtopper.service.PublicMarketingStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class PublicMarketingStatsController {

    private final PublicMarketingStatsService publicMarketingStatsService;

    @GetMapping
    public PublicMarketingStatsResponse stats() {
        return publicMarketingStatsService.snapshot();
    }
}
