-- Move architect expertise and portfolio project type onto the IAI (Ikatan Arsitek Indonesia)
-- building classification. Data only -- both columns already hold free text wide enough for the
-- new codes (the longest is 27 characters).
--
-- The mapping is best-effort. Old values with no IAI counterpart (Sustainable Design, Urban
-- Planning, Interior Design, Landscape Architecture, Other, and the project-taxonomy codes
-- INTERIOR_ONLY / LANDSCAPE / INFRASTRUCTURE) are deliberately left untouched rather than
-- dropped: the frontend renders them as inert grey "legacy" chips, and the backend grandfathers
-- them so an affected profile can still be saved. Nothing is lost.

-- rmtr_porto.project_type holds a single value. It has been fed from two different lists: the old
-- PROJECT_TYPES picker, and -- when a finished project was archived into a portfolio -- raw
-- project-taxonomy category codes. Both are mapped here.
UPDATE rmtr_porto
SET project_type = CASE project_type
    WHEN 'Residential - Single Family' THEN 'K3_HUNIAN'
    WHEN 'RESIDENTIAL'                 THEN 'K3_HUNIAN'
    WHEN 'Residential - Multi Family'  THEN 'K2_HUNIAN'
    WHEN 'Commercial - Office'         THEN 'K2_KOMERSIAL'
    WHEN 'Commercial - Retail'         THEN 'K2_KOMERSIAL'
    WHEN 'COMMERCIAL'                  THEN 'K2_KOMERSIAL'
    WHEN 'MIXED_USE'                   THEN 'K2_KOMERSIAL'
    WHEN 'Hospitality'                 THEN 'K3_KOMERSIAL'
    WHEN 'Institutional'               THEN 'K2_KOMUNITAS'
    WHEN 'INSTITUTIONAL'               THEN 'K2_KOMUNITAS'
    WHEN 'Cultural'                    THEN 'K3_KOMUNITAS'
    WHEN 'Renovation'                  THEN 'K3_LAINNYA'
    WHEN 'INDUSTRIAL'                  THEN 'K2_INDUSTRI'
    ELSE project_type
  END
WHERE project_type IS NOT NULL;

-- rmtr_architect.expertise is a JSONB array, and one old tag can fan out to several IAI pairs
-- (an architect who said "Residential" works across Kategori 1, 2 and 3). Each array is rebuilt
-- from its elements; unmapped elements pass through unchanged, and DISTINCT collapses the
-- overlaps created by the fan-out.
WITH mapping(old_tag, new_codes) AS (
  VALUES
    ('Residential',          ARRAY['K1_HUNIAN', 'K2_HUNIAN', 'K3_HUNIAN']),
    ('Commercial',           ARRAY['K1_KOMERSIAL', 'K2_KOMERSIAL']),
    ('Renovation',           ARRAY['K3_LAINNYA']),
    ('Historic Preservation', ARRAY['K3_LAINNYA']),
    ('Institutional',        ARRAY['K2_KOMUNITAS', 'K2_PENDIDIKAN']),
    ('Industrial',           ARRAY['K1_INDUSTRI', 'K2_INDUSTRI']),
    ('Mixed-Use',            ARRAY['K2_KOMERSIAL']),
    ('Hospitality',          ARRAY['K3_KOMERSIAL'])
),
rebuilt AS (
  SELECT a.id,
         jsonb_agg(DISTINCT translated.code) AS expertise
  FROM rmtr_architect a
  CROSS JOIN LATERAL jsonb_array_elements_text(a.expertise) AS tag(value)
  CROSS JOIN LATERAL (
    SELECT unnest(m.new_codes) AS code FROM mapping m WHERE m.old_tag = tag.value
    UNION ALL
    SELECT tag.value WHERE NOT EXISTS (SELECT 1 FROM mapping m WHERE m.old_tag = tag.value)
  ) AS translated
  WHERE a.expertise IS NOT NULL
    AND jsonb_typeof(a.expertise) = 'array'
  GROUP BY a.id
)
UPDATE rmtr_architect a
SET expertise = rebuilt.expertise
FROM rebuilt
WHERE a.id = rebuilt.id
  AND a.expertise IS DISTINCT FROM rebuilt.expertise;
