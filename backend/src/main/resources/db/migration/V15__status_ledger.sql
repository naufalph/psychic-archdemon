-- Append-only status ledger for the money-touching streams.
--
-- The log is the source of truth for history; each entity's `status` column remains a
-- projection of it, written in the same transaction. Authorization reads the projection
-- (lockable, constrainable); audit reads the log.
--
-- NOTE: any future migration that alters these tables must drop and recreate the
-- append-only triggers below, which reject UPDATE, DELETE and TRUNCATE.

CREATE OR REPLACE FUNCTION rmtr_reject_mutation() RETURNS trigger AS $$
BEGIN
  RAISE EXCEPTION 'append-only table %: % is not permitted', TG_TABLE_NAME, TG_OP;
END;
$$ LANGUAGE plpgsql;


CREATE TABLE rmtr_project_status_log (
    id          BIGSERIAL PRIMARY KEY,
    project_id  BIGINT NOT NULL REFERENCES rmtr_project(id),
    actor_id    BIGINT REFERENCES rmtr_user(id),
    actor_type  VARCHAR(50) NOT NULL,
    action      VARCHAR(100) NOT NULL,
    from_status VARCHAR(50),
    to_status   VARCHAR(50) NOT NULL,
    metadata    JSONB,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_project_status_log_to CHECK (to_status IN (
        'DRAFT','PENDING_APPROVAL','REJECTED','OPEN','BIDDING_CLOSED','NEGOTIATION',
        'NEGOTIATION_EXPIRED','IN_PROGRESS','COMPLETED','CANCELLED','DELETED'))
);
CREATE INDEX idx_project_status_log_project_id ON rmtr_project_status_log(project_id);

CREATE TABLE rmtr_phase_payment_status_log (
    id               BIGSERIAL PRIMARY KEY,
    phase_payment_id BIGINT NOT NULL REFERENCES rmtr_phase_payment(id),
    actor_id         BIGINT REFERENCES rmtr_user(id),
    actor_type       VARCHAR(50) NOT NULL,
    action           VARCHAR(100) NOT NULL,
    from_status      VARCHAR(50),
    to_status        VARCHAR(50) NOT NULL,
    metadata         JSONB,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_phase_payment_status_log_to CHECK (to_status IN (
        'PENDING','COMPLETED','FAILED','EXPIRED'))
);
CREATE INDEX idx_phase_payment_status_log_payment_id
    ON rmtr_phase_payment_status_log(phase_payment_id);

CREATE TABLE rmtr_phase_disbursement_status_log (
    id              BIGSERIAL PRIMARY KEY,
    disbursement_id BIGINT NOT NULL REFERENCES rmtr_project_phase_disbursement(id),
    actor_id        BIGINT REFERENCES rmtr_user(id),
    actor_type      VARCHAR(50) NOT NULL,
    action          VARCHAR(100) NOT NULL,
    from_status     VARCHAR(50),
    to_status       VARCHAR(50) NOT NULL,
    metadata        JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_phase_disbursement_status_log_to CHECK (to_status IN (
        'PENDING','ACCEPTED','SUCCEEDED','FAILED','REVERSED'))
);
CREATE INDEX idx_phase_disbursement_status_log_disbursement_id
    ON rmtr_phase_disbursement_status_log(disbursement_id);

CREATE TABLE rmtr_token_purchase_status_log (
    id                BIGSERIAL PRIMARY KEY,
    token_purchase_id BIGINT NOT NULL REFERENCES rmtr_token_purchase(id),
    actor_id          BIGINT REFERENCES rmtr_user(id),
    actor_type        VARCHAR(50) NOT NULL,
    action            VARCHAR(100) NOT NULL,
    from_status       VARCHAR(50),
    to_status         VARCHAR(50) NOT NULL,
    metadata          JSONB,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_token_purchase_status_log_to CHECK (to_status IN (
        'PENDING','COMPLETED','FAILED','EXPIRED','CANCELLED'))
);
CREATE INDEX idx_token_purchase_status_log_purchase_id
    ON rmtr_token_purchase_status_log(token_purchase_id);

CREATE TABLE rmtr_subscription_status_log (
    id              BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL REFERENCES rmtr_subscription(id),
    actor_id        BIGINT REFERENCES rmtr_user(id),
    actor_type      VARCHAR(50) NOT NULL,
    action          VARCHAR(100) NOT NULL,
    from_status     VARCHAR(50),
    to_status       VARCHAR(50) NOT NULL,
    metadata        JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_subscription_status_log_to CHECK (to_status IN (
        'ACTIVE','PENDING','EXPIRED','CANCELLED'))
);
CREATE INDEX idx_subscription_status_log_subscription_id
    ON rmtr_subscription_status_log(subscription_id);


-- Seed a starting point per live entity. History before this migration does not exist and
-- is deliberately not fabricated.
INSERT INTO rmtr_project_status_log (project_id, actor_type, action, to_status)
    SELECT id, 'SYSTEM', 'LEDGER_INITIALIZED', status FROM rmtr_project;
INSERT INTO rmtr_phase_payment_status_log (phase_payment_id, actor_type, action, to_status)
    SELECT id, 'SYSTEM', 'LEDGER_INITIALIZED', status FROM rmtr_phase_payment;
INSERT INTO rmtr_phase_disbursement_status_log (disbursement_id, actor_type, action, to_status)
    SELECT id, 'SYSTEM', 'LEDGER_INITIALIZED', status FROM rmtr_project_phase_disbursement;
INSERT INTO rmtr_token_purchase_status_log (token_purchase_id, actor_type, action, to_status)
    SELECT id, 'SYSTEM', 'LEDGER_INITIALIZED', status FROM rmtr_token_purchase;
INSERT INTO rmtr_subscription_status_log (subscription_id, actor_type, action, to_status)
    SELECT id, 'SYSTEM', 'LEDGER_INITIALIZED', COALESCE(status, 'ACTIVE') FROM rmtr_subscription;


-- Append-only enforcement. Row-level triggers do not fire on TRUNCATE, so each table needs
-- a statement-level TRUNCATE trigger as well.
DO $$
DECLARE
  t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY[
    'rmtr_project_status_log',
    'rmtr_phase_payment_status_log',
    'rmtr_phase_disbursement_status_log',
    'rmtr_token_purchase_status_log',
    'rmtr_subscription_status_log',
    'rmtr_project_phase_log'
  ] LOOP
    EXECUTE format(
      'CREATE TRIGGER %I_append_only BEFORE UPDATE OR DELETE ON %I
         FOR EACH ROW EXECUTE FUNCTION rmtr_reject_mutation()', t, t);
    EXECUTE format(
      'CREATE TRIGGER %I_no_truncate BEFORE TRUNCATE ON %I
         FOR EACH STATEMENT EXECUTE FUNCTION rmtr_reject_mutation()', t, t);
  END LOOP;
END $$;


-- Database-level backstop against a second live payout for one phase. FAILED/REVERSED are
-- excluded so the existing retry flow keeps working.
CREATE UNIQUE INDEX idx_phase_disbursement_active
    ON rmtr_project_phase_disbursement(phase_id)
    WHERE status NOT IN ('FAILED', 'REVERSED');
