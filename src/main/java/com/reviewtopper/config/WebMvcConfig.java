package com.reviewtopper.config;

import org.springframework.context.annotation.Configuration;

/** CORS is configured only via {@link GlobalCorsConfig} + Spring Security to avoid conflicting rules. */
@Configuration
public class WebMvcConfig {
}
