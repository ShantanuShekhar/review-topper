-- Upgrade legacy MySQL JSON native columns to LONGTEXT (MariaDB-compatible text JSON).
-- Safe to MARK_RAN when columns are already LONGTEXT (fresh installs use V1 with LONGTEXT).

ALTER TABLE subscription_plans
    MODIFY COLUMN features_json LONGTEXT NOT NULL;

ALTER TABLE workspaces
    MODIFY COLUMN theme_config LONGTEXT NOT NULL;

ALTER TABLE customer_interactions
    MODIFY COLUMN metadata_json LONGTEXT NULL;
