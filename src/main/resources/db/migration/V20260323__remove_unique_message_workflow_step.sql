-- Allow reusing the same message in multiple workflow steps.
DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    SELECT c.conname INTO constraint_name
    FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    JOIN unnest(c.conkey) AS colnum ON true
    JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = colnum
    WHERE n.nspname = 'komflow'
      AND t.relname = 'msg_event_registration_workflow_steps'
      AND a.attname = 'msg_message_id'
      AND c.contype = 'u'
    LIMIT 1;

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE komflow.msg_event_registration_workflow_steps DROP CONSTRAINT %I', constraint_name);
    END IF;
END $$;

DO $$
DECLARE
    index_name TEXT;
BEGIN
    SELECT i.relname INTO index_name
    FROM pg_index idx
    JOIN pg_class i ON i.oid = idx.indexrelid
    JOIN pg_class t ON t.oid = idx.indrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    JOIN unnest(idx.indkey) AS colnum ON true
    JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = colnum
    WHERE n.nspname = 'komflow'
      AND t.relname = 'msg_event_registration_workflow_steps'
      AND a.attname = 'msg_message_id'
      AND idx.indisunique = true
    LIMIT 1;

    IF index_name IS NOT NULL THEN
        EXECUTE format('DROP INDEX IF EXISTS %I.%I', 'komflow', index_name);
    END IF;
END $$;
