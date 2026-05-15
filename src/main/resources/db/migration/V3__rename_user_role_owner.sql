-- SaaS terminology: subscribers are OWNERS of their workspaces (not generic USER).
UPDATE users SET role = 'OWNER' WHERE role = 'USER';
