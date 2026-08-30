-- Suppression complete de la fonctionnalite d'audit log applicatif : cette
-- responsabilite est desormais assuree par un service mesh externe.
-- On supprime d'abord la table de jonction prs_users_logs (issue du
-- @OneToMany User.logs) car elle porte des FK vers aut_audit_logs, puis la
-- table d'audit elle-meme et sa sequence.

DROP TABLE IF EXISTS prs_users_logs;

DROP TABLE IF EXISTS aut_audit_logs;

DROP SEQUENCE IF EXISTS aut_audit_logs_seq;
