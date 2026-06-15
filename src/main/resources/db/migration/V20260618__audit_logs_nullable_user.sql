-- Allow anonymous audit log entries (e.g. failed signup attempts, public API calls).
-- connected_user_id can be NULL when the request is not authenticated.
ALTER TABLE komflow.aut_audit_logs
    ALTER COLUMN connected_user_id DROP NOT NULL;
