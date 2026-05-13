ALTER TABLE rmtr_project_phase
    ADD COLUMN max_revisions INTEGER NOT NULL DEFAULT 3,
    ADD COLUMN revisions_used INTEGER NOT NULL DEFAULT 0;
