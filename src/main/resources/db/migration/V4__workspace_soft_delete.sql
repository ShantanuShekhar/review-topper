ALTER TABLE workspaces
    ADD COLUMN is_active BIT(1) NOT NULL DEFAULT b'1' AFTER button_style;

UPDATE workspaces SET is_active = b'1' WHERE is_active IS NULL;

CREATE INDEX idx_workspaces_active_owner_created ON workspaces (is_active, owner_id, created_at);
CREATE INDEX idx_workspaces_active_slug ON workspaces (is_active, slug);
