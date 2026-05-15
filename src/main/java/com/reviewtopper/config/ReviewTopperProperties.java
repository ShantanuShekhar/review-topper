package com.reviewtopper.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "review-topper")
public class ReviewTopperProperties {

    private final Jwt jwt = new Jwt();
    private final Cors cors = new Cors();
    private final PublicUrls publicUrls = new PublicUrls();
    private final Auth auth = new Auth();
    private final Bootstrap bootstrap = new Bootstrap();
    private final Ai ai = new Ai();

    @Data
    public static class Ai {
        private boolean enabled = true;
        @Positive
        private long timeoutMs = 1800L;
    }

    @Data
    public static class Jwt {
        @NotBlank
        private String secret = "change-me-to-a-long-random-secret-at-least-256-bits-for-hs512";
        @Positive
        private long expirationMs = 86_400_000L;
    }

    @Data
    public static class Cors {
        /** Comma-separated origins */
        private String allowedOrigins = "http://localhost:3000,http://localhost:5173";
    }

    @Data
    public static class PublicUrls {
        /** Backend origin — e.g. <code>GET /redirect/{slug}</code> must hit the API host. */
        private String apiBaseUrl = "http://localhost:8080";
        /** SPA origin — QR codes and customer-facing <code>/r/{slug}</code> links open here. */
        private String frontendBaseUrl = "http://localhost:5173";
    }

    @Data
    public static class Auth {
        /** Dev/demo only; never enable in production without replacing email delivery */
        private boolean exposeResetTokenInResponse = false;
    }

    @Data
    public static class Bootstrap {
        private String adminEmail = "admin@reviewtopper.local";
        private String adminPassword = "ChangeMeAdmin!1";
        private String adminName = "Platform Admin";
    }
}
