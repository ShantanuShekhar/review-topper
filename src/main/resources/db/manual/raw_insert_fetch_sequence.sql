-- =============================================================================
-- Review Topper — raw INSERT then FETCH queries in dependency-safe sequence
-- Engine: MySQL 8.x / MariaDB 10.6+ / InnoDB (matches Liquibase V1 schema; JSON as LONGTEXT strings)
-- Replace placeholders like :plan_id / literals such as 1L with real IDs after inserts.
-- Password column must hold a BCrypt hash (Spring BCryptPasswordEncoder), not plain text.
-- =============================================================================

SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- SECTION A — INSERT (respect foreign keys: parents before children)
-- -----------------------------------------------------------------------------

-- A1. subscription_plans (no FK)
INSERT INTO subscription_plans (
    name,
    duration_type,
    max_workspaces,
    price,
    features_json,
    active
) VALUES (
    'Starter',
    'MONTHLY',
    1,
    0.00,
    '{"tier":"starter","themeCustomization":true}',
    b'1'
);

INSERT INTO subscription_plans (
    name,
    duration_type,
    max_workspaces,
    price,
    features_json,
    active
) VALUES (
    'Growth',
    'MONTHLY',
    5,
    49.00,
    '{"tier":"growth","workspaceLimit":5}',
    b'1'
);

-- Capture plan IDs if needed:
-- SET @starter_plan_id = LAST_INSERT_ID();  -- after Starter insert only if single-statement batch

-- A2. users (no FK)
INSERT INTO users (
    name,
    email,
    phone,
    password,
    is_verified,
    role
) VALUES (
    'Demo Owner',
    'owner@example.com',
    '+15555550100',
    '$2a$12$REPLACE_WITH_BCRYPT_HASH_OF_PASSWORD',
    b'1',
    'OWNER'
);

SET @demo_user_id = LAST_INSERT_ID();

INSERT INTO users (
    name,
    email,
    phone,
    password,
    is_verified,
    role
) VALUES (
    'Platform Admin',
    'admin@example.com',
    NULL,
    '$2a$12$REPLACE_WITH_BCRYPT_HASH_OF_ADMIN_PASSWORD',
    1,
    'ADMIN'
);

SET @admin_user_id = LAST_INSERT_ID();

-- A3. password_reset_tokens (FK → users)
INSERT INTO password_reset_tokens (
    user_id,
    token_hash,
    expires_at,
    used_at
) VALUES (
    @demo_user_id,
    'sha256_hex_of_reset_token_here_64_chars MaxLen128___________________________________________',
    DATE_ADD(UTC_TIMESTAMP(), INTERVAL 1 HOUR),
    NULL
);

-- A4. subscriptions (FK → users, subscription_plans)
INSERT INTO subscriptions (
    user_id,
    plan_id,
    start_date,
    end_date,
    active
) VALUES (
    @demo_user_id,
    (SELECT id FROM subscription_plans WHERE name = 'Starter' LIMIT 1),
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 50 YEAR),
    b'1'
);

-- A5. workspaces (FK → users.owner_id)
INSERT INTO workspaces (
    slug,
    name,
    business_type,
    logo_url,
    google_review_link,
    phone,
    owner_id,
    theme_config,
    button_style
) VALUES (
    'demo-wellness-co',
    'Demo Wellness Collective',
    'CLINIC',
    'https://cdn.example.com/logos/demo.png',
    'https://maps.google.com/?cid=REPLACE_REVIEW_LINK',
    '+15555550101',
    @demo_user_id,
    '{"primaryColor":"#2563eb","secondaryColor":"#64748b","darkModeEnabled":false,"logoPosition":"TOP_CENTER","accentColor":"#0ea5e9"}',
    'ROUNDED'
);

SET @demo_workspace_id = LAST_INSERT_ID();

-- A6. customer_interactions (FK → workspaces)
INSERT INTO customer_interactions (
    workspace_id,
    source_type,
    status,
    metadata_json
) VALUES (
    @demo_workspace_id,
    'LANDING_PAGE',
    'VISIT',
    '{"locale":"en"}'
);

INSERT INTO customer_interactions (
    workspace_id,
    source_type,
    status,
    metadata_json
) VALUES (
    @demo_workspace_id,
    'REVIEW_REDIRECT',
    'REDIRECT_INITIATED',
    NULL
);

-- A7. feedbacks (FK → workspaces)
INSERT INTO feedbacks (
    workspace_id,
    message,
    sentiment
) VALUES (
    @demo_workspace_id,
    'Wait time was longer than expected.',
    'NEGATIVE'
);

-- A8. dynamic_labels — global (workspace_id NULL)
INSERT INTO dynamic_labels (
    workspace_id,
    label_key,
    label_value,
    category,
    locale
) VALUES (
    NULL,
    'cta_primary_review',
    'Tell us how we did',
    'cta',
    'en'
);

-- A8b. dynamic_labels — workspace-scoped override
INSERT INTO dynamic_labels (
    workspace_id,
    label_key,
    label_value,
    category,
    locale
) VALUES (
    @demo_workspace_id,
    'cta_primary_review',
    'Rate your visit today',
    'cta',
    'en'
);

-- A9. whatsapp_message_templates — global (workspace_id NULL)
INSERT INTO whatsapp_message_templates (
    template_key,
    workspace_id,
    message_template,
    active
) VALUES (
    'review_invite',
    NULL,
    'Hi! If you visited {{business_name}}, we''d love a quick review: {{review_link}}\n\nFeedback page: {{public_landing_url}}',
    b'1'
);

-- A9b. whatsapp_message_templates — workspace-specific (optional)
INSERT INTO whatsapp_message_templates (
    template_key,
    workspace_id,
    message_template,
    active
) VALUES (
    'review_invite',
    @demo_workspace_id,
    'Thanks for choosing {{business_name}} — review us here: {{review_link}}',
    b'1'
);

-- -----------------------------------------------------------------------------
-- SECTION B — FETCH (SELECT) — typical reads aligned with app usage order
-- -----------------------------------------------------------------------------

-- B1. Fetch user by email (login lookup)
SELECT id, name, email, phone, password, is_verified, role, created_at, updated_at
FROM users
WHERE LOWER(email) = LOWER('owner@example.com');

-- B2. Fetch active subscription plans (public pricing API)
SELECT id, name, duration_type, max_workspaces, price, features_json, active, created_at, updated_at
FROM subscription_plans
WHERE active = 1
ORDER BY price ASC;

-- B3. Fetch effective subscription for a user (today inside [start_date, end_date], active)
SELECT s.id,
       s.user_id,
       s.plan_id,
       s.start_date,
       s.end_date,
       s.active,
       p.name AS plan_name,
       p.max_workspaces,
       p.price,
       p.features_json
FROM subscriptions s
JOIN subscription_plans p ON p.id = s.plan_id
WHERE s.user_id = @demo_user_id
  AND s.active = 1
  AND CURDATE() BETWEEN s.start_date AND s.end_date
ORDER BY s.end_date DESC
LIMIT 1;

-- B4. Count workspaces owned by user (quota enforcement)
SELECT COUNT(*) AS workspace_count
FROM workspaces
WHERE owner_id = @demo_user_id;

-- B5. List workspaces for owner
SELECT id, slug, name, business_type, logo_url, google_review_link, phone, owner_id,
       theme_config, button_style, created_at, updated_at
FROM workspaces
WHERE owner_id = @demo_user_id
ORDER BY created_at DESC;

-- B6. Fetch workspace by slug (public landing)
SELECT id, slug, name, business_type, logo_url, google_review_link, phone, owner_id,
       theme_config, button_style, created_at, updated_at
FROM workspaces
WHERE LOWER(slug) = LOWER('demo-wellness-co');

-- B7. Global labels + merged fetch pattern (globals then overrides — application merges by key)
SELECT id, workspace_id, label_key, label_value, category, locale, created_at, updated_at
FROM dynamic_labels
WHERE workspace_id IS NULL AND locale = 'en'
ORDER BY COALESCE(category, ''), label_key;

SELECT id, workspace_id, label_key, label_value, category, locale, created_at, updated_at
FROM dynamic_labels
WHERE workspace_id = @demo_workspace_id AND locale = 'en'
ORDER BY COALESCE(category, ''), label_key;

-- B8. WhatsApp template resolution (workspace-specific wins over global in app logic)
SELECT id, template_key, workspace_id, message_template, active, created_at, updated_at
FROM whatsapp_message_templates
WHERE template_key = 'review_invite'
  AND active = 1
  AND (workspace_id IS NULL OR workspace_id = @demo_workspace_id)
ORDER BY CASE WHEN workspace_id IS NOT NULL THEN 0 ELSE 1 END;

-- B9. Analytics window counts — visits / redirects (replace start/end instants)
SELECT COUNT(*) AS visits
FROM customer_interactions
WHERE workspace_id = @demo_workspace_id
  AND source_type = 'LANDING_PAGE'
  AND status = 'VISIT'
  AND created_at >= UTC_TIMESTAMP() - INTERVAL 30 DAY
  AND created_at < UTC_TIMESTAMP();

SELECT COUNT(*) AS redirects
FROM customer_interactions
WHERE workspace_id = @demo_workspace_id
  AND source_type = 'REVIEW_REDIRECT'
  AND status = 'REDIRECT_INITIATED'
  AND created_at >= UTC_TIMESTAMP() - INTERVAL 30 DAY
  AND created_at < UTC_TIMESTAMP();

-- B10. Feedback sentiment totals in window
SELECT sentiment, COUNT(*) AS cnt
FROM feedbacks
WHERE workspace_id = @demo_workspace_id
  AND created_at >= UTC_TIMESTAMP() - INTERVAL 30 DAY
  AND created_at < UTC_TIMESTAMP()
GROUP BY sentiment;

-- B11. Daily trend — interactions (matches repository-style aggregation)
SELECT DATE(created_at) AS day,
       SUM(CASE WHEN source_type = 'LANDING_PAGE' AND status = 'VISIT' THEN 1 ELSE 0 END) AS visits,
       SUM(CASE WHEN source_type = 'REVIEW_REDIRECT' AND status = 'REDIRECT_INITIATED' THEN 1 ELSE 0 END) AS redirects
FROM customer_interactions
WHERE workspace_id = @demo_workspace_id
  AND created_at >= UTC_TIMESTAMP() - INTERVAL 30 DAY
  AND created_at < UTC_TIMESTAMP()
GROUP BY DATE(created_at)
ORDER BY day;

-- B12. Daily trend — feedback by sentiment
SELECT DATE(created_at) AS day,
       SUM(CASE WHEN sentiment = 'POSITIVE' THEN 1 ELSE 0 END) AS happy,
       SUM(CASE WHEN sentiment = 'NEUTRAL' THEN 1 ELSE 0 END) AS neutral,
       SUM(CASE WHEN sentiment = 'NEGATIVE' THEN 1 ELSE 0 END) AS bad
FROM feedbacks
WHERE workspace_id = @demo_workspace_id
  AND created_at >= UTC_TIMESTAMP() - INTERVAL 30 DAY
  AND created_at < UTC_TIMESTAMP()
GROUP BY DATE(created_at)
ORDER BY day;

-- B13. Password reset token lookup (application hashes plaintext token before lookup)
SELECT prt.id, prt.user_id, prt.token_hash, prt.expires_at, prt.used_at, prt.created_at
FROM password_reset_tokens prt
WHERE prt.token_hash = 'sha256_hex_of_supplied_reset_token';

-- =============================================================================
-- End of sequence
-- =============================================================================
