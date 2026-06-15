-- Flyway V20260615__add_billing_plans.sql
-- Tables de gestion des plans SaaS et des quotas d'usage

CREATE TABLE IF NOT EXISTS komflow.bil_plans (
    code                   VARCHAR(50)  PRIMARY KEY,
    label                  VARCHAR(100) NOT NULL,
    price_monthly_cts_cents BIGINT NOT NULL DEFAULT 0,
    max_contacts_total     INT     NOT NULL DEFAULT 500,
    max_campaigns_per_month INT    NOT NULL DEFAULT 2,
    max_emails_per_month   INT     NOT NULL DEFAULT 1000,
    max_sms_per_month      INT     NOT NULL DEFAULT 0,
    max_whatsapp_per_month INT     NOT NULL DEFAULT 0,
    max_users_per_org      INT     NOT NULL DEFAULT 1,
    can_use_whatsapp       BOOLEAN NOT NULL DEFAULT FALSE,
    can_use_sms            BOOLEAN NOT NULL DEFAULT FALSE,
    can_use_workflows      BOOLEAN NOT NULL DEFAULT FALSE,
    can_use_advanced_rbac  BOOLEAN NOT NULL DEFAULT FALSE
);

-- Plans standards
INSERT INTO komflow.bil_plans VALUES
  ('FREE',       'Gratuit',    0,       500,  2,   1000,    0,     0,    1, false, false, false, false),
  ('STARTER',    'Starter',  2900,    2000,  10,  10000,  500,   200,    3,  true,  true, false, false),
  ('PRO',        'Pro',       9900,   10000,  50, 100000, 5000,  2000,  10,  true,  true,  true, false),
  ('ENTERPRISE', 'Enterprise',    0,    -1,  -1,    -1,    -1,    -1,  -1,  true,  true,  true,  true)
ON CONFLICT (code) DO NOTHING;

CREATE TABLE IF NOT EXISTS komflow.bil_subscriptions (
    id                BIGSERIAL PRIMARY KEY,
    organization_id   BIGINT NOT NULL UNIQUE REFERENCES komflow.org_organizations(id),
    plan_code         VARCHAR(50) NOT NULL DEFAULT 'FREE',
    status            VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    current_period_end TIMESTAMPTZ,
    external_id       VARCHAR(255),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS komflow.bil_usage_counters (
    organization_id BIGINT       NOT NULL REFERENCES komflow.org_organizations(id),
    metric          VARCHAR(50)  NOT NULL,
    year_month      VARCHAR(6)   NOT NULL,
    count           BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (organization_id, metric, year_month)
);

CREATE INDEX IF NOT EXISTS idx_usage_org_month ON komflow.bil_usage_counters (organization_id, year_month);

-- Abonnement ENTERPRISE pour l'organisation initiale
INSERT INTO komflow.bil_subscriptions (organization_id, plan_code, status)
SELECT id, 'ENTERPRISE', 'ACTIVE'
  FROM komflow.org_organizations
 WHERE slug = 'kompozith'
ON CONFLICT (organization_id) DO NOTHING;
