-- The survey keeps one brief per visit and reports how far it got, so drop-off can be counted
-- per step. project_id ties a claimed brief to the draft its survey produced; projects are never
-- hard-deleted (see the status ledger), so the reference stays valid.
ALTER TABLE rmtr_landing_brief
    ADD COLUMN furthest_step VARCHAR(20),
    ADD COLUMN project_id    BIGINT REFERENCES rmtr_project (id),
    ADD COLUMN updated_at    TIMESTAMP;

CREATE INDEX idx_landing_brief_project_id ON rmtr_landing_brief (project_id);
