-- Passage de la relation Message→Event de ManyToOne vers ManyToMany.
-- Création de la table de jointure, migration des données existantes,
-- puis suppression de l'ancienne colonne FK.

-- 1. Création de la table de jointure
CREATE TABLE IF NOT EXISTS komflow.msg_message_events (
    msg_message_id BIGINT NOT NULL,
    msg_event_id   BIGINT NOT NULL,
    PRIMARY KEY (msg_message_id, msg_event_id),
    CONSTRAINT fk_mme_message FOREIGN KEY (msg_message_id)
        REFERENCES komflow.msg_messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_mme_event FOREIGN KEY (msg_event_id)
        REFERENCES komflow.msg_events(id) ON DELETE CASCADE
);

-- 2. Migration des associations existantes (si la colonne existe encore)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'komflow'
          AND table_name   = 'msg_messages'
          AND column_name  = 'msg_event_id'
    ) THEN
        INSERT INTO komflow.msg_message_events (msg_message_id, msg_event_id)
        SELECT id, msg_event_id
        FROM komflow.msg_messages
        WHERE msg_event_id IS NOT NULL
        ON CONFLICT DO NOTHING;
    END IF;
END $$;

-- 3. Suppression de l'ancienne colonne FK
ALTER TABLE komflow.msg_messages DROP COLUMN IF EXISTS msg_event_id;

