-- V20260616__add_organization_members.sql
-- Table de gestion des membres par espace (multi-workspace)

CREATE TABLE IF NOT EXISTS komflow.org_members (
    id                     BIGSERIAL PRIMARY KEY,
    organization_id        BIGINT NOT NULL REFERENCES komflow.org_organizations(id) ON DELETE CASCADE,
    user_id                BIGINT NOT NULL REFERENCES komflow.prs_users(id) ON DELETE CASCADE,
    role                   VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    status                 VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    invited_email          VARCHAR(255),
    invite_token           VARCHAR(64) UNIQUE,
    invite_token_expires_at TIMESTAMPTZ,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by             VARCHAR(255),
    updated_by             VARCHAR(255),
    CONSTRAINT uq_org_member UNIQUE (organization_id, user_id)
);

-- Permissions supplémentaires par membre
CREATE TABLE IF NOT EXISTS komflow.org_member_permissions (
    member_id  BIGINT NOT NULL REFERENCES komflow.org_members(id) ON DELETE CASCADE,
    permission VARCHAR(100) NOT NULL,
    PRIMARY KEY (member_id, permission)
);

CREATE INDEX IF NOT EXISTS idx_org_members_org  ON komflow.org_members (organization_id);
CREATE INDEX IF NOT EXISTS idx_org_members_user ON komflow.org_members (user_id);
CREATE INDEX IF NOT EXISTS idx_org_members_token ON komflow.org_members (invite_token) WHERE invite_token IS NOT NULL;

-- Migrer les utilisateurs existants comme OWNER de l'organisation Kompozith
INSERT INTO komflow.org_members (organization_id, user_id, role, status)
SELECT
    o.id,
    u.id,
    'OWNER',
    'ACTIVE'
FROM komflow.org_organizations o
CROSS JOIN komflow.prs_users u
WHERE o.slug = 'kompozith'
ON CONFLICT (organization_id, user_id) DO NOTHING;
