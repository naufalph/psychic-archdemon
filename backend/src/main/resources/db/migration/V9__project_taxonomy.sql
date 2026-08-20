ALTER TABLE rmtr_project
    ADD COLUMN project_scope VARCHAR(20),
    ADD COLUMN sub_category VARCHAR(60);

ALTER TABLE rmtr_landing_preset
    ADD COLUMN project_scope VARCHAR(20) NOT NULL DEFAULT 'NEW_BUILD',
    ADD COLUMN sub_category VARCHAR(60);

ALTER TABLE rmtr_landing_brief
    ADD COLUMN project_scope VARCHAR(20),
    ADD COLUMN sub_category VARCHAR(60);

-- The old single-field values (RESIDENTIAL/STUDENT_HOUSING/VILLA/COMMERCIAL/RENOVATION)
-- mixed work type, category and building subtype, so none of them survives as a category.
UPDATE rmtr_project SET building_function = NULL;

UPDATE rmtr_landing_preset SET project_scope = 'NEW_BUILD', building_function = 'RESIDENTIAL', sub_category = 'HOUSE' WHERE slug = 'residential';
UPDATE rmtr_landing_preset SET project_scope = 'NEW_BUILD', building_function = 'RESIDENTIAL', sub_category = 'BOARDING_HOUSE' WHERE slug = 'student-housing';
UPDATE rmtr_landing_preset SET project_scope = 'NEW_BUILD', building_function = 'RESIDENTIAL', sub_category = 'VILLA' WHERE slug = 'villa';
UPDATE rmtr_landing_preset SET project_scope = 'NEW_BUILD', building_function = 'COMMERCIAL', sub_category = 'OFFICE' WHERE slug = 'commercial';
UPDATE rmtr_landing_preset SET project_scope = 'RENOVATION', building_function = 'RESIDENTIAL', sub_category = 'HOUSE' WHERE slug = 'renovation';
