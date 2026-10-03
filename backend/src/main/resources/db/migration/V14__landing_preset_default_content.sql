-- The seeded presets shipped without default content, so a "Tipe Proyek" card opened a blank
-- brief. Seed starting values the superuser can edit in the Preset Editor; COALESCE keeps
-- anything already written there.
UPDATE rmtr_landing_preset AS p
SET default_title_en       = COALESCE(p.default_title_en, v.title_en),
    default_title_id       = COALESCE(p.default_title_id, v.title_id),
    default_description_en = COALESCE(p.default_description_en, v.desc_en),
    default_description_id = COALESCE(p.default_description_id, v.desc_id),
    default_lot_size       = COALESCE(p.default_lot_size, v.lot_size),
    default_design_budget  = COALESCE(p.default_design_budget, v.budget)
FROM (VALUES
    ('residential',
     'Two-storey family home', 'Rumah keluarga dua lantai',
     'Three bedrooms, an open living and dining area, a carport and a small garden.',
     'Tiga kamar tidur, ruang keluarga dan makan terbuka, carport, serta taman kecil.',
     150, 45000000),
    ('student-housing',
     'Boarding house (kost) near campus', 'Kost dekat kampus',
     'Around 12 rooms with en-suite bathrooms, a shared kitchen and motorbike parking.',
     'Sekitar 12 kamar dengan kamar mandi dalam, dapur bersama, dan parkir motor.',
     300, 90000000),
    ('villa',
     'Holiday villa with a pool', 'Villa liburan dengan kolam renang',
     'Three bedrooms, an open-plan living area facing the view, and a private pool.',
     'Tiga kamar tidur, ruang keluarga terbuka menghadap pemandangan, dan kolam renang pribadi.',
     400, 120000000),
    ('commercial',
     'Small office building', 'Gedung kantor kecil',
     'Three floors of open-plan office space, meeting rooms, a lobby and on-site parking.',
     'Tiga lantai ruang kantor terbuka, ruang rapat, lobi, dan area parkir.',
     500, 150000000),
    ('renovation',
     'Home renovation', 'Renovasi rumah',
     'Rework the layout of an existing house, add a room, and refresh the facade.',
     'Mengubah tata ruang rumah yang ada, menambah satu kamar, dan memperbarui fasad.',
     120, 25000000)
) AS v (slug, title_en, title_id, desc_en, desc_id, lot_size, budget)
WHERE p.slug = v.slug;
