-- Tags each uploaded file to one of the deliverables named in the accepted bid, and records
-- which of those deliverables the client has approved.
--
-- The deliverable list itself is NOT copied here -- it stays owned by
-- rmtr_bid_payment_phase.deliverables. Files reference a position in that array rather than a
-- name, because names are not stable: a rename or a duplicate would silently regroup files.

ALTER TABLE rmtr_project_phase_deliverable
    ADD COLUMN deliverable_index INT;

CREATE INDEX idx_phase_deliverable_index
    ON rmtr_project_phase_deliverable(phase_id, deliverable_index);

COMMENT ON COLUMN rmtr_project_phase_deliverable.deliverable_index IS
    'Position in the accepted bid phase''s deliverables array. NULL for files uploaded before '
    'tagging existed; those render under "Other files".';


-- Current-state projection of the DELIVERABLE_APPROVED events in rmtr_project_phase_log.
-- The log is the durable record and is append-only; rows here are cleared when a revision is
-- requested, which erases no history.
--
-- The unique constraint is what makes double-approval impossible, so the approve path needs no
-- lock -- this is the path that flips a phase to APPROVED and unlocks the architect's payout.
CREATE TABLE rmtr_project_phase_deliverable_approval (
    id                BIGSERIAL PRIMARY KEY,
    phase_id          BIGINT NOT NULL REFERENCES rmtr_project_phase(id),
    deliverable_index INT NOT NULL,
    approved_by       BIGINT REFERENCES rmtr_user(id),
    approved_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_phase_deliverable_approval UNIQUE (phase_id, deliverable_index)
);

CREATE INDEX idx_phase_deliverable_approval_phase_id
    ON rmtr_project_phase_deliverable_approval(phase_id);
