-- Suppression definitive du concept de "username" : l'email devient le seul
-- identifiant d'authentification (login/signup). Voir prs_persons.email ci-dessous,
-- desormais garanti unique au niveau base de donnees.

ALTER TABLE prs_users
    DROP CONSTRAINT uc_prs_users_username;

ALTER TABLE prs_users
    DROP COLUMN username;

ALTER TABLE prs_persons
    ADD CONSTRAINT uc_prs_persons_email UNIQUE (email);
