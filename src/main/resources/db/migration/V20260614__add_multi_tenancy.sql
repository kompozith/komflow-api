-- Flyway V20260614__add_multi_tenancy.sql
-- Création de la table des organisations (tenants)

CREATE TABLE IF NOT EXISTS komflow.org_organizations (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    slug          VARCHAR(100) UNIQUE NOT NULL,
    plan_code     VARCHAR(50)  NOT NULL DEFAULT 'FREE',
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    trial_ends_at TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255)
);

-- Organisation initiale (Kompozith — données existantes)
INSERT INTO komflow.org_organizations (name, slug, plan_code, active)
VALUES ('Kompozith', 'kompozith', 'ENTERPRISE', true)
ON CONFLICT (slug) DO NOTHING;

-- Ajout de la colonne organization_id sur les tables métier
-- Nullable d'abord pour permettre la migration des données existantes

ALTER TABLE komflow.cnt_contacts
    ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES komflow.org_organizations(id);

ALTER TABLE komflow.cnt_tags
    ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES komflow.org_organizations(id);

ALTER TABLE komflow.msg_messages
    ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES komflow.org_organizations(id);

ALTER TABLE komflow.msg_campaigns
    ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES komflow.org_organizations(id);

ALTER TABLE komflow.msg_events
    ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES komflow.org_organizations(id);

ALTER TABLE komflow.prs_users
    ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES komflow.org_organizations(id);

-- Migration des données existantes vers l'organisation Kompozith (id = 1)
UPDATE komflow.cnt_contacts  SET organization_id = (SELECT id FROM komflow.org_organizations WHERE slug = 'kompozith') WHERE organization_id IS NULL;
UPDATE komflow.cnt_tags       SET organization_id = (SELECT id FROM komflow.org_organizations WHERE slug = 'kompozith') WHERE organization_id IS NULL;
UPDATE komflow.msg_messages   SET organization_id = (SELECT id FROM komflow.org_organizations WHERE slug = 'kompozith') WHERE organization_id IS NULL;
UPDATE komflow.msg_campaigns  SET organization_id = (SELECT id FROM komflow.org_organizations WHERE slug = 'kompozith') WHERE organization_id IS NULL;
UPDATE komflow.msg_events     SET organization_id = (SELECT id FROM komflow.org_organizations WHERE slug = 'kompozith') WHERE organization_id IS NULL;
UPDATE komflow.prs_users      SET organization_id = (SELECT id FROM komflow.org_organizations WHERE slug = 'kompozith') WHERE organization_id IS NULL;

-- Index pour les requêtes filtrées par organisation
CREATE INDEX IF NOT EXISTS idx_contacts_org   ON komflow.cnt_contacts  (organization_id);
CREATE INDEX IF NOT EXISTS idx_tags_org       ON komflow.cnt_tags       (organization_id);
CREATE INDEX IF NOT EXISTS idx_messages_org   ON komflow.msg_messages   (organization_id);
CREATE INDEX IF NOT EXISTS idx_campaigns_org  ON komflow.msg_campaigns  (organization_id);
CREATE INDEX IF NOT EXISTS idx_events_org     ON komflow.msg_events     (organization_id);
CREATE INDEX IF NOT EXISTS idx_users_org      ON komflow.prs_users      (organization_id);
