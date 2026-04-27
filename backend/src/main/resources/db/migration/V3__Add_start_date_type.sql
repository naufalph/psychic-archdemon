ALTER TABLE rmtr_project
    ADD COLUMN start_date_type VARCHAR(20) NOT NULL DEFAULT 'IMMEDIATELY';

ALTER TABLE rmtr_project
    ADD CONSTRAINT chk_start_date_type
        CHECK (start_date_type IN ('IMMEDIATELY', 'SPECIFIC_DATE'));
