-- Landing "Tipe Proyek" cards render each preset's photo; three sizes match the hero-slide image set.
ALTER TABLE rmtr_landing_preset
    ADD COLUMN image_original_url TEXT,
    ADD COLUMN image_large_url TEXT,
    ADD COLUMN image_medium_url TEXT;
