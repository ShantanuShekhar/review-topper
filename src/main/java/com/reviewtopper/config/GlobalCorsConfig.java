package com.reviewtopper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class GlobalCorsConfig {

    private static final List<String> DEFAULT_ORIGINS = List.of(
            "https://reviewtopper.me",
            "https://www.reviewtopper.me"
    );

    @Bean
    public CorsConfigurationSource corsConfigurationSource(ReviewTopperProperties properties) {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(resolveOrigins(properties.getCors().getAllowedOrigins()));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    static List<String> resolveOrigins(String allowedOrigins) {
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            return DEFAULT_ORIGINS;
        }
        List<String> parsed = parseOrigins(allowedOrigins).stream()
                .filter(origin -> !"*".equals(origin))
                .toList();
        return parsed.isEmpty() ? DEFAULT_ORIGINS : parsed;
    }

    static List<String> parseOrigins(String raw) {
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
