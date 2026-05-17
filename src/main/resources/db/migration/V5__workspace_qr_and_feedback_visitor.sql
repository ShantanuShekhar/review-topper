-- Persist QR (PNG as Base64) on workspace; visitor-scoped feedback lock; interaction submitted flag
ALTER TABLE workspaces
    ADD COLUMN qr_code_base64 MEDIUMTEXT NULL;

ALTER TABLE customer_interactions
    ADD COLUMN is_submitted BIT(1) NOT NULL DEFAULT b'0';

ALTER TABLE feedbacks
    ADD COLUMN visitor_submission_key VARCHAR(64) NULL AFTER sentiment;

CREATE UNIQUE INDEX uk_fb_workspace_visitor ON feedbacks (workspace_id, visitor_submission_key);
