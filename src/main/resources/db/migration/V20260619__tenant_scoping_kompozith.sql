-- ══════════════════════════════════════════════════════════════════════════════
-- V20260619 — Tenant scoping : espace Kompozith + colonne organization_id
-- sur toutes les tables de données métier
-- ══════════════════════════════════════════════════════════════════════════════

-- ── 1. Créer l'espace (organisation) "Kompozith" s'il n'existe pas ───────────
INSERT INTO komflow.org_organizations (name, slug, plan_code, active, created_at, updated_at)
SELECT 'Kompozith', 'kompozith', 'FREE', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM komflow.org_organizations WHERE slug = 'kompozith');

-- Référence partagée dans tout le script
DO $$
DECLARE
    org_id BIGINT;
BEGIN
    SELECT id INTO org_id FROM komflow.org_organizations WHERE slug = 'kompozith' LIMIT 1;

    -- ── 2. Ajouter organization_id sur chaque table métier ──────────────────

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

    ALTER TABLE komflow.core_file
        ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES komflow.org_organizations(id);

    -- ── 3. Assigner toutes les données existantes à Kompozith ───────────────
    UPDATE komflow.cnt_contacts  SET organization_id = org_id WHERE organization_id IS NULL;
    UPDATE komflow.cnt_tags      SET organization_id = org_id WHERE organization_id IS NULL;
    UPDATE komflow.msg_messages  SET organization_id = org_id WHERE organization_id IS NULL;
    UPDATE komflow.msg_campaigns SET organization_id = org_id WHERE organization_id IS NULL;
    UPDATE komflow.msg_events    SET organization_id = org_id WHERE organization_id IS NULL;
    UPDATE komflow.core_file     SET organization_id = org_id WHERE organization_id IS NULL;

    -- ── 4. Passer la colonne en NOT NULL après backfill ─────────────────────
    ALTER TABLE komflow.cnt_contacts  ALTER COLUMN organization_id SET NOT NULL;
    ALTER TABLE komflow.cnt_tags      ALTER COLUMN organization_id SET NOT NULL;
    ALTER TABLE komflow.msg_messages  ALTER COLUMN organization_id SET NOT NULL;
    ALTER TABLE komflow.msg_campaigns ALTER COLUMN organization_id SET NOT NULL;
    ALTER TABLE komflow.msg_events    ALTER COLUMN organization_id SET NOT NULL;
    ALTER TABLE komflow.core_file     ALTER COLUMN organization_id SET NOT NULL;

    -- ── 5. Index pour les requêtes filtrées par org ──────────────────────────
    CREATE INDEX IF NOT EXISTS idx_cnt_contacts_org  ON komflow.cnt_contacts(organization_id);
    CREATE INDEX IF NOT EXISTS idx_cnt_tags_org      ON komflow.cnt_tags(organization_id);
    CREATE INDEX IF NOT EXISTS idx_msg_messages_org  ON komflow.msg_messages(organization_id);
    CREATE INDEX IF NOT EXISTS idx_msg_campaigns_org ON komflow.msg_campaigns(organization_id);
    CREATE INDEX IF NOT EXISTS idx_msg_events_org    ON komflow.msg_events(organization_id);
    CREATE INDEX IF NOT EXISTS idx_core_file_org     ON komflow.core_file(organization_id);

    -- ── 6. Ajouter tous les users existants comme MEMBER de Kompozith ───────
    INSERT INTO komflow.org_members (organization_id, user_id, role, status, created_at, updated_at)
    SELECT org_id, u.id, 'MEMBER', 'ACTIVE', NOW(), NOW()
    FROM komflow.prs_users u
    WHERE NOT EXISTS (
        SELECT 1 FROM komflow.org_members m
        WHERE m.organization_id = org_id AND m.user_id = u.id
    );

    -- Promouvoir le premier user en OWNER s'il n'y a pas encore de OWNER
    UPDATE komflow.org_members
    SET role = 'OWNER'
    WHERE organization_id = org_id
      AND user_id = (SELECT id FROM komflow.prs_users ORDER BY id LIMIT 1)
      AND NOT EXISTS (
          SELECT 1 FROM komflow.org_members WHERE organization_id = org_id AND role = 'OWNER'
      );
END $$;
