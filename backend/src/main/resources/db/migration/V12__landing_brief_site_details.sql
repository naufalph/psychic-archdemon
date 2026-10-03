-- The brief form now captures the same site details as the project wizard, so a brief can
-- prefill the wizard without the client re-entering the address, map pin or build area.
ALTER TABLE rmtr_landing_brief ALTER COLUMN location TYPE VARCHAR(255);

ALTER TABLE rmtr_landing_brief
    ADD COLUMN city       VARCHAR(255),
    ADD COLUMN province   VARCHAR(100),
    ADD COLUMN latitude   NUMERIC(10, 7),
    ADD COLUMN longitude  NUMERIC(10, 7),
    ADD COLUMN build_area INTEGER;
