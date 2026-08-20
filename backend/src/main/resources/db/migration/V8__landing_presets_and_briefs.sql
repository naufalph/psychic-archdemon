CREATE TABLE rmtr_landing_preset (
    id                     BIGSERIAL PRIMARY KEY,
    slug                   VARCHAR(60)  NOT NULL UNIQUE,
    label_en               VARCHAR(120) NOT NULL,
    label_id               VARCHAR(120) NOT NULL,
    eyebrow_en             VARCHAR(60),
    eyebrow_id             VARCHAR(60),
    icon_name              VARCHAR(40)  NOT NULL DEFAULT 'Home',
    building_function      VARCHAR(40)  NOT NULL,
    default_title_en       VARCHAR(160),
    default_title_id       VARCHAR(160),
    default_lot_size       INT,
    default_design_budget  BIGINT,
    default_description_en TEXT,
    default_description_id TEXT,
    display_order          INT          NOT NULL DEFAULT 0,
    is_active              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at             TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP
);

CREATE INDEX idx_landing_preset_order ON rmtr_landing_preset (display_order);

-- Seeds mirror the previously hardcoded STARTERS array in LandingPage.vue so the
-- landing page renders identically on first boot after this migration.
INSERT INTO rmtr_landing_preset
    (slug, label_en, label_id, eyebrow_en, eyebrow_id, icon_name, building_function, display_order)
VALUES ('residential', 'Residential Home', 'Rumah Tinggal', 'Residential', 'Hunian', 'Home', 'RESIDENTIAL', 1),
       ('student-housing', 'Student Housing (Kost)', 'Kost / Hunian Mahasiswa', 'Residential', 'Hunian', 'Building2', 'STUDENT_HOUSING', 2),
       ('villa', 'Villa / Resort', 'Villa / Resort', 'Residential', 'Hunian', 'Palmtree', 'VILLA', 3),
       ('commercial', 'Commercial / Office', 'Komersial / Kantor', 'Commercial', 'Komersial', 'Briefcase', 'COMMERCIAL', 4),
       ('renovation', 'Renovation', 'Renovasi', 'Other', 'Lainnya', 'Hammer', 'RENOVATION', 5);

CREATE TABLE rmtr_landing_brief (
    id                  BIGSERIAL PRIMARY KEY,
    claim_token         VARCHAR(64)  NOT NULL UNIQUE,
    preset_id           BIGINT REFERENCES rmtr_landing_preset (id) ON DELETE SET NULL,
    building_function   VARCHAR(40),
    title               VARCHAR(160),
    location            VARCHAR(160),
    lot_size            INT,
    design_budget_total BIGINT,
    design_budget_min   BIGINT,
    design_budget_max   BIGINT,
    description         TEXT,
    phone_number        VARCHAR(24),
    start_date_type     VARCHAR(20),
    expected_start_date DATE,
    claimed_by_user_id  BIGINT REFERENCES rmtr_user (id) ON DELETE SET NULL,
    claimed_at          TIMESTAMP,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_landing_brief_claim_token ON rmtr_landing_brief (claim_token);
