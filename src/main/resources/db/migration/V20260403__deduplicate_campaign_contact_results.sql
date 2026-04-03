-- ============================================================
-- Déduplication de msg_campaign_contact_results
--   + contrainte d'unicité (campaign_id, contact_id)
--
-- Règle de fusion des doublons :
--   Pour chaque paire (campaign_id, contact_id) on ne garde
--   qu'UNE ligne, choisie selon la priorité suivante :
--     1. Statut SUCCESS  (priorité maximale)
--     2. Date created_at la plus récente
--     3. id le plus grand (tie-breaker)
--
-- Après nettoyage, la contrainte unique est ajoutée pour
-- empêcher toute future insertion de doublon.
-- ============================================================

-- Étape 1 : supprimer les doublons en gardant la meilleure ligne
DELETE FROM komflow.msg_campaign_contact_results
WHERE id NOT IN (
    SELECT DISTINCT ON (campaign_id, contact_id) id
    FROM komflow.msg_campaign_contact_results
    ORDER BY
        campaign_id,
        contact_id,
        -- SUCCESS < FAILED  (valeur 0 = meilleur)
        CASE WHEN status = 'SUCCESS' THEN 0 ELSE 1 END  ASC,
        created_at DESC NULLS LAST,
        id          DESC
);

-- Étape 2 : supprimer l'ancien index simple sur campaign_id s'il existe sous
--           son nom généré par Hibernate (la contrainte unique le remplace)
DROP INDEX IF EXISTS komflow.idx_ccr_campaign_id;
DROP INDEX IF EXISTS komflow.idx_ccr_campaign_id_status;

-- Étape 3 : ajouter la contrainte d'unicité (campaign_id, contact_id)
ALTER TABLE komflow.msg_campaign_contact_results
    DROP CONSTRAINT IF EXISTS uq_ccr_campaign_contact;

ALTER TABLE komflow.msg_campaign_contact_results
    ADD CONSTRAINT uq_ccr_campaign_contact
    UNIQUE (campaign_id, contact_id);

-- Étape 4 : recréer les index de performance (couverts par la contrainte
--           unique pour campaign_id seul, mais on les recrée explicitement
--           pour les requêtes filtrées par status)
CREATE INDEX IF NOT EXISTS idx_ccr_campaign_id
    ON komflow.msg_campaign_contact_results (campaign_id);

CREATE INDEX IF NOT EXISTS idx_ccr_campaign_status
    ON komflow.msg_campaign_contact_results (campaign_id, status);

