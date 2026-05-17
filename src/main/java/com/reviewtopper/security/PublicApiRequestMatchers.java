package com.reviewtopper.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

/** Public API paths — kept in sync with {@link SecurityConfig} permitAll rules. */
final class PublicApiRequestMatchers {

    private PublicApiRequestMatchers() {
    }

    static List<RequestMatcher> all() {
        return List.of(
                new AntPathRequestMatcher("/", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/actuator/**"),
                new AntPathRequestMatcher("/api/auth/test", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/auth/register", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/auth/login", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/auth/forgot-password", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/auth/reset-password", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/subscription-plans", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/subscription-plans/**", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/plans", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/plans/**", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/stats", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/stats/**", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/feedback", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/generate-comments", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/submit-feedback", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/public/**", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/redirect/**", HttpMethod.GET.name())
                
        );
    }
}
