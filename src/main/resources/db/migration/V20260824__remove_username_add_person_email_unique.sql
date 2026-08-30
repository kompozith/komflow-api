-- Suppression definitive du concept de "username" : l'email devient le seul
-- identifiant d'authentification (login/signup). Voir prs_persons.email ci-dessous,
-- desormais garanti unique au niveau base de donnees.

DO $$
BEGIN
    ALTER TABLE prs_users
        DROP CONSTRAINT uc_prs_users_username;
EXCEPTION
    WHEN undefined_object THEN NULL;
END $$;

ALTER TABLE prs_users
    DROP COLUMN IF EXISTS username;

DO $$
BEGIN
    ALTER TABLE prs_persons
        ADD CONSTRAINT uc_prs_persons_email UNIQUE (email);
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;
