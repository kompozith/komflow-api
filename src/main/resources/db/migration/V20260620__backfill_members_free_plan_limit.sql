-- ─────────────────────────────────────────────────────────────────────────────
-- V20260620 : deux corrections
--   1. Ajouter les users non encore membres de l'espace "kompozith" comme MEMBER
--   2. Réduire le quota d'emails du plan FREE : 1000 → 200
-- ─────────────────────────────────────────────────────────────────────────────

-- ── 1. Backfill membres Kompozith ──────────────────────────────────────────
DO $$
DECLARE
    org_id BIGINT;
BEGIN
    SELECT id INTO org_id
    FROM komflow.org_organizations
    WHERE slug = 'kompozith'
    LIMIT 1;

    IF org_id IS NULL THEN
        RAISE NOTICE 'Organisation "kompozith" introuvable, backfill ignoré.';
        RETURN;
    END IF;

    -- Insérer tous les users qui n'ont pas encore de ligne dans org_members
    -- pour cet espace, en tant que MEMBER actif.
    INSERT INTO komflow.org_members (organization_id, user_id, role, status, created_at, updated_at)
    SELECT org_id, u.id, 'MEMBER', 'ACTIVE', NOW(), NOW()
    FROM komflow.prs_users u
    WHERE NOT EXISTS (
        SELECT 1
        FROM komflow.org_members m
        WHERE m.organization_id = org_id
          AND m.user_id = u.id
    );

    -- Promouvoir le premier user (id le plus petit) en OWNER
    -- si aucun OWNER n'existe encore dans cet espace.
    UPDATE komflow.org_members
    SET role = 'OWNER'
    WHERE organization_id = org_id
      AND user_id = (SELECT id FROM komflow.prs_users ORDER BY id LIMIT 1)
      AND NOT EXISTS (
          SELECT 1
          FROM komflow.org_members
          WHERE organization_id = org_id AND role = 'OWNER'
      );
END $$;

-- ── 2. Plan FREE : max emails par mois 1000 → 200 ──────────────────────────
UPDATE komflow.bil_plans
SET max_emails_per_month = 200
WHERE code = 'FREE';
