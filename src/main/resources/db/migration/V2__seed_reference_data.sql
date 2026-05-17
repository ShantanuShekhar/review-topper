-- Idempotent catalog seed (MySQL + MariaDB). JSON values are plain strings (LONGTEXT columns).
-- INSERT ... SELECT ... WHERE NOT EXISTS — no FROM DUAL required.

INSERT INTO subscription_plans (name, duration_type, max_workspaces, price, features_json, active)
SELECT 'Starter',
       'MONTHLY',
       1,
       0.00,
       '{"tier":"starter","qrDownloads":"unlimited","themeCustomization":true,"labelsEditable":false}',
       b'1'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Starter');

INSERT INTO subscription_plans (name, duration_type, max_workspaces, price, features_json, active)
SELECT 'Growth',
       'MONTHLY',
       5,
       49.00,
       '{"tier":"growth","workspaceLimit":5,"themeCustomization":true,"labelsEditable":true,"prioritySupport":false}',
       b'1'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Growth');

INSERT INTO subscription_plans (name, duration_type, max_workspaces, price, features_json, active)
SELECT 'Growth Annual',
       'YEARLY',
       5,
       490.00,
       '{"tier":"growth","billing":"annual","workspaceLimit":5,"discountMonths":2}',
       b'1'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Growth Annual');

INSERT INTO subscription_plans (name, duration_type, max_workspaces, price, features_json, active)
SELECT 'Enterprise',
       'MONTHLY',
       999,
       299.00,
       '{"tier":"enterprise","workspaceLimit":999,"sso":false,"auditLogs":true,"support":"priority"}',
       b'1'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Enterprise');

INSERT INTO dynamic_labels (workspace_id, label_key, label_value, category, locale)
SELECT NULL, 'cta_primary_review', 'Share your experience', 'cta', 'en'
WHERE NOT EXISTS (
    SELECT 1 FROM dynamic_labels WHERE workspace_id IS NULL AND label_key = 'cta_primary_review' AND locale = 'en'
);

INSERT INTO dynamic_labels (workspace_id, label_key, label_value, category, locale)
SELECT NULL, 'cta_secondary_feedback', 'Something went wrong? Tell us privately.', 'cta', 'en'
WHERE NOT EXISTS (
    SELECT 1 FROM dynamic_labels WHERE workspace_id IS NULL AND label_key = 'cta_secondary_feedback' AND locale = 'en'
);

INSERT INTO dynamic_labels (workspace_id, label_key, label_value, category, locale)
SELECT NULL, 'business_generic_descriptor', 'business', 'copy', 'en'
WHERE NOT EXISTS (
    SELECT 1 FROM dynamic_labels WHERE workspace_id IS NULL AND label_key = 'business_generic_descriptor' AND locale = 'en'
);

INSERT INTO whatsapp_message_templates (template_key, workspace_id, message_template, active)
SELECT 'review_invite',
       NULL,
       'Hi! If you enjoyed visiting {{business_name}}, we''d appreciate a quick review: {{review_link}}

You can also use our feedback page: {{public_landing_url}}',
       b'1'
WHERE NOT EXISTS (
    SELECT 1
    FROM whatsapp_message_templates
    WHERE template_key = 'review_invite' AND workspace_id IS NULL
);
