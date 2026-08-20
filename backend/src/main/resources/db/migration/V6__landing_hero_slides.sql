CREATE TABLE rmtr_landing_hero_slide (
    id                 BIGSERIAL PRIMARY KEY,
    image_original_url TEXT,
    image_large_url    TEXT,
    image_medium_url   TEXT,
    architect_name     VARCHAR(120)  NOT NULL,
    avatar_initial     VARCHAR(2),
    verified           BOOLEAN       NOT NULL DEFAULT TRUE,
    rating             NUMERIC(2, 1),
    review_quote       TEXT,
    reviewer_name      VARCHAR(120),
    display_order      INT           NOT NULL DEFAULT 0,
    is_active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP
);

CREATE INDEX idx_landing_hero_slide_order ON rmtr_landing_hero_slide (display_order);

INSERT INTO rmtr_landing_hero_slide (architect_name, avatar_initial, verified, display_order)
VALUES ('Studio Atelier', 'A', TRUE, 1),
       ('Ruang Karya', 'R', TRUE, 2),
       ('Nusa Design Lab', 'N', TRUE, 3);
