package com.reviewtopper.service;

import com.reviewtopper.config.ReviewTopperProperties;
import com.reviewtopper.entity.DynamicLabel;
import com.reviewtopper.entity.SubscriptionPlan;
import com.reviewtopper.entity.User;
import com.reviewtopper.entity.WhatsAppMessageTemplate;
import com.reviewtopper.enums.DurationType;
import com.reviewtopper.enums.UserRole;
import com.reviewtopper.repository.DynamicLabelRepository;
import com.reviewtopper.repository.SubscriptionPlanRepository;
import com.reviewtopper.repository.UserRepository;
import com.reviewtopper.repository.WhatsAppMessageTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@Order(10)
@RequiredArgsConstructor
@Slf4j
public class BootstrapRunner implements ApplicationRunner {

    private final SubscriptionService subscriptionService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final DynamicLabelRepository dynamicLabelRepository;
    private final WhatsAppMessageTemplateRepository whatsAppMessageTemplateRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReviewTopperProperties properties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedPlansIfEmpty();
        seedLabelsIfEmpty();
        seedWhatsAppTemplateIfEmpty();
        seedAdminIfNeeded();
        log.info("Catalog bootstrap finished (plans present: {}).", subscriptionPlanRepository.count());
    }

    private void seedPlansIfEmpty() {
        if (subscriptionPlanRepository.count() > 0) {
            return;
        }
        subscriptionPlanRepository.save(SubscriptionPlan.builder()
                .name("Starter")
                .durationType(DurationType.MONTHLY)
                .maxWorkspaces(1)
                .price(BigDecimal.ZERO)
                .featuresJson(
                        "{\"tier\":\"starter\",\"qrDownloads\":\"unlimited\",\"themeCustomization\":true,\"labelsEditable\":false}")
                .active(true)
                .build());

        subscriptionPlanRepository.save(SubscriptionPlan.builder()
                .name("Growth")
                .durationType(DurationType.MONTHLY)
                .maxWorkspaces(5)
                .price(new BigDecimal("49.00"))
                .featuresJson(
                        "{\"tier\":\"growth\",\"workspaceLimit\":5,\"themeCustomization\":true,\"labelsEditable\":true,\"prioritySupport\":false}")
                .active(true)
                .build());

        subscriptionPlanRepository.save(SubscriptionPlan.builder()
                .name("Growth Annual")
                .durationType(DurationType.YEARLY)
                .maxWorkspaces(5)
                .price(new BigDecimal("490.00"))
                .featuresJson(
                        "{\"tier\":\"growth\",\"billing\":\"annual\",\"workspaceLimit\":5,\"discountMonths\":2}")
                .active(true)
                .build());

        subscriptionPlanRepository.save(SubscriptionPlan.builder()
                .name("Enterprise")
                .durationType(DurationType.MONTHLY)
                .maxWorkspaces(999)
                .price(new BigDecimal("299.00"))
                .featuresJson(
                        "{\"tier\":\"enterprise\",\"workspaceLimit\":999,\"sso\":false,\"auditLogs\":true,\"support\":\"priority\"}")
                .active(true)
                .build());
    }

    private void seedLabelsIfEmpty() {
        if (dynamicLabelRepository.count() > 0) {
            return;
        }
        dynamicLabelRepository.save(DynamicLabel.builder()
                .workspace(null)
                .labelKey("cta_primary_review")
                .labelValue("Share your experience")
                .category("cta")
                .locale("en")
                .build());
        dynamicLabelRepository.save(DynamicLabel.builder()
                .workspace(null)
                .labelKey("cta_secondary_feedback")
                .labelValue("Something went wrong? Tell us privately.")
                .category("cta")
                .locale("en")
                .build());
        dynamicLabelRepository.save(DynamicLabel.builder()
                .workspace(null)
                .labelKey("business_generic_descriptor")
                .labelValue("business")
                .category("copy")
                .locale("en")
                .build());
    }

    private void seedWhatsAppTemplateIfEmpty() {
        if (whatsAppMessageTemplateRepository.count() > 0) {
            return;
        }
        whatsAppMessageTemplateRepository.save(WhatsAppMessageTemplate.builder()
                .templateKey(WhatsAppLinkService.DEFAULT_TEMPLATE_KEY)
                .workspace(null)
                .messageTemplate(
                        "Hi! If you enjoyed visiting {{business_name}}, we'd appreciate a quick review: {{review_link}}\n\nYou can also use our feedback page: {{public_landing_url}}")
                .active(true)
                .build());
    }

    private void seedAdminIfNeeded() {
        String email = properties.getBootstrap().getAdminEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        User admin = User.builder()
                .name(properties.getBootstrap().getAdminName())
                .email(email)
                .password(passwordEncoder.encode(properties.getBootstrap().getAdminPassword()))
                .verified(true)
                .role(UserRole.ADMIN)
                .build();
        userRepository.save(admin);
        subscriptionService.assignStarterSubscription(admin);
        log.warn("Created bootstrap ADMIN user {}; change BOOTSTRAP_ADMIN_PASSWORD immediately.", email);
    }
}
