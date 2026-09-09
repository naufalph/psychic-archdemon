-- The signed agreement behind a project, and the two signatures on it.
--
-- Until now the workspace's Contract & Payment tab was a pure read model, recomputed on every
-- request from the accepted bid. Nothing recorded what the two parties actually agreed to, so
-- there was no artifact to point at when a phase went to dispute.
--
-- rmtr_project_contract is a snapshot, taken once per project the first time either party opens
-- the contract, of the accepted bid's terms: total fee, timeline, and every phase with its
-- amount, deliverables and revision rounds. terms_snapshot is the structured form; body_en and
-- body_id are the two renderings of it. Both bodies come from the same snapshot, so content_hash
-- is computed over the snapshot rather than over either body -- the two parties provably sign the
-- same terms whichever language they read.
--
-- Both tables are append-only. A contract that could be edited after signing would be worth
-- nothing as evidence, so UPDATE, DELETE and TRUNCATE are rejected by trigger. Nothing in the
-- application may call a setter on a loaded ProjectContract inside a transaction: Hibernate
-- dirty-checking would flush an UPDATE and hit the trigger.
--
-- Any future migration that alters these tables must drop and recreate both triggers.

CREATE TABLE rmtr_project_contract (
    id               BIGSERIAL PRIMARY KEY,
    project_id       BIGINT NOT NULL REFERENCES rmtr_project(id),
    bid_id           BIGINT NOT NULL REFERENCES rmtr_bid(id),
    template_version VARCHAR(16) NOT NULL,
    terms_snapshot   JSONB NOT NULL,
    body_en          TEXT NOT NULL,
    body_id          TEXT NOT NULL,
    content_hash     VARCHAR(64) NOT NULL,
    generated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_project_contract_project UNIQUE (project_id)
);

CREATE TABLE rmtr_project_contract_acceptance (
    id             BIGSERIAL PRIMARY KEY,
    contract_id    BIGINT NOT NULL REFERENCES rmtr_project_contract(id),
    project_id     BIGINT NOT NULL REFERENCES rmtr_project(id),
    user_id        BIGINT NOT NULL REFERENCES rmtr_user(id),
    party          VARCHAR(16) NOT NULL,
    signature_name VARCHAR(255) NOT NULL,
    content_hash   VARCHAR(64) NOT NULL,
    lang           VARCHAR(8) NOT NULL,
    accepted_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address     VARCHAR(64),
    user_agent     TEXT,
    CONSTRAINT chk_contract_acceptance_party CHECK (party IN ('CLIENT', 'ARCHITECT')),
    CONSTRAINT uq_contract_acceptance_party UNIQUE (contract_id, party)
);

CREATE INDEX idx_project_contract_acceptance_project
    ON rmtr_project_contract_acceptance(project_id);

COMMENT ON COLUMN rmtr_project_contract.terms_snapshot IS
    'Structured copy of the accepted bid''s terms at snapshot time. The bid can change afterwards; '
    'this cannot, and it is what was signed.';

COMMENT ON COLUMN rmtr_project_contract.content_hash IS
    'SHA-256 over the canonical terms_snapshot JSON prefixed by template_version. Language '
    'independent by design, so the en and id renderings share one hash.';

COMMENT ON COLUMN rmtr_project_contract_acceptance.project_id IS
    'Denormalised from the contract so signatures can be looked up by project without a join; '
    'the unique constraint on rmtr_project_contract.project_id keeps the two in step.';

COMMENT ON COLUMN rmtr_project_contract_acceptance.signature_name IS
    'Stored exactly as the signer typed it. Matching against the account name is done on a '
    'normalised copy, never by rewriting what they entered.';

COMMENT ON COLUMN rmtr_project_contract_acceptance.lang IS
    'Which rendering the signer was shown. Evidence of what they read, not what they agreed to.';

DO $$
DECLARE
  t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY[
    'rmtr_project_contract',
    'rmtr_project_contract_acceptance'
  ] LOOP
    EXECUTE format(
      'CREATE TRIGGER %I_append_only BEFORE UPDATE OR DELETE ON %I
         FOR EACH ROW EXECUTE FUNCTION rmtr_reject_mutation()', t, t);
    EXECUTE format(
      'CREATE TRIGGER %I_no_truncate BEFORE TRUNCATE ON %I
         FOR EACH STATEMENT EXECUTE FUNCTION rmtr_reject_mutation()', t, t);
  END LOOP;
END $$;
