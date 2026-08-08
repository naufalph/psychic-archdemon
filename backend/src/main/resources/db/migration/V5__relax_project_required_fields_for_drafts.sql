-- Projects can now be saved as DRAFT before the client fills in required fields.
ALTER TABLE rmtr_project ALTER COLUMN title DROP NOT NULL;
ALTER TABLE rmtr_project ALTER COLUMN location DROP NOT NULL;
