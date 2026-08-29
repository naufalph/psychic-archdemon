ALTER TABLE rmtr_project
    ADD COLUMN full_address TEXT,
    ADD COLUMN city         VARCHAR(255),
    ADD COLUMN province     VARCHAR(100),
    ADD COLUMN latitude     NUMERIC(10,7),
    ADD COLUMN longitude    NUMERIC(10,7);
