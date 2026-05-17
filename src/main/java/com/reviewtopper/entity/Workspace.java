package com.reviewtopper.entity;

import com.reviewtopper.enums.BusinessType;
import com.reviewtopper.enums.ButtonStyle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "workspaces")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 160)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", nullable = false, length = 64)
    private BusinessType businessType;

    @Column(name = "logo_url", length = 2048)
    private String logoUrl;

    @Column(name = "google_review_link", length = 2048)
    private String googleReviewLink;

    @Column(length = 64)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "theme_config", nullable = false, columnDefinition = "LONGTEXT")
    private ThemeConfiguration themeConfig;

    @Enumerated(EnumType.STRING)
    @Column(name = "button_style", length = 32)
    private ButtonStyle buttonStyle;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    /** PNG bytes Base64-encoded at 640px; regenerated when slug/theme affects QR. */
    @Column(name = "qr_code_base64", columnDefinition = "MEDIUMTEXT")
    private String qrCodeBase64;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        active = true;
        if (themeConfig == null) {
            themeConfig = ThemeConfiguration.builder()
                    .primaryColor("#2563eb")
                    .secondaryColor("#64748b")
                    .darkModeEnabled(false)
                    .logoPosition(com.reviewtopper.enums.LogoPosition.TOP_CENTER)
                    .accentColor("#0ea5e9")
                    .build();
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
