-- Records what the client asked to be redone, per deliverable.
--
-- The revision *count* stays on rmtr_project_phase (revisions_used / max_revisions): one request
-- covers however many deliverables the client selected and costs exactly one round. revision_round
-- is what pools them -- every row written by a single request shares it.
--
-- Rows are never deleted. Unlike the approval table, this is not a projection of anything: it is
-- the durable record of the instructions the architect has to work from, and previous rounds stay
-- readable as history. The phase log still carries the single REVISION_REQUESTED transition.

CREATE TABLE rmtr_project_phase_deliverable_revision (
    id                BIGSERIAL PRIMARY KEY,
    phase_id          BIGINT NOT NULL REFERENCES rmtr_project_phase(id),
    deliverable_index INT NOT NULL,
    revision_round    INT NOT NULL,
    notes             TEXT NOT NULL,
    requested_by      BIGINT REFERENCES rmtr_user(id),
    requested_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_phase_deliverable_revision UNIQUE (phase_id, deliverable_index, revision_round)
);

CREATE INDEX idx_phase_deliverable_revision_phase_id
    ON rmtr_project_phase_deliverable_revision(phase_id);

COMMENT ON COLUMN rmtr_project_phase_deliverable_revision.deliverable_index IS
    'Position in the accepted bid phase''s deliverables array, same convention as '
    'rmtr_project_phase_deliverable.deliverable_index.';

COMMENT ON COLUMN rmtr_project_phase_deliverable_revision.revision_round IS
    'phase.revisions_used after the request incremented it. Rows sharing a round were requested '
    'together and cost one round between them.';
