-- ── Password Reset OTP tokens ────────────────────────────────────────────────
-- Un token à usage unique (OTP 6 chiffres) avec TTL de 15 minutes.
-- Suppression automatique après utilisation ou expiration (via application).

CREATE TABLE IF NOT EXISTS komflow.auth_password_reset_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES komflow.prs_users(id) ON DELETE CASCADE,
    otp_code    VARCHAR(6)   NOT NULL,
    reset_token VARCHAR(64)  UNIQUE,           -- token long pour l'étape 3 (changement mdp)
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_prt_user_id    ON komflow.auth_password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_prt_otp_code   ON komflow.auth_password_reset_tokens(otp_code);
CREATE INDEX IF NOT EXISTS idx_prt_reset_token ON komflow.auth_password_reset_tokens(reset_token);
