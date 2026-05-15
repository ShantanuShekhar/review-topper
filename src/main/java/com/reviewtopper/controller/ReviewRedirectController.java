package com.reviewtopper.controller;

import com.reviewtopper.service.WorkspaceService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/redirect")
@RequiredArgsConstructor
public class ReviewRedirectController {

    private final WorkspaceService workspaceService;

    @GetMapping("/{workspaceSlug}")
    public void redirect(@PathVariable String workspaceSlug, HttpServletResponse response) throws IOException {
        String target = workspaceService.redirectToReviewClick(workspaceSlug);
        response.sendRedirect(target);
    }
}
