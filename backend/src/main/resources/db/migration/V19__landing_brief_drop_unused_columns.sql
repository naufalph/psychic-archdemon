-- The brief is now only the survey's funnel record: the answers given after login live on the
-- project draft, and nothing reads a brief back into a form. These columns came from the old
-- landing mini-form and are no longer written.
ALTER TABLE rmtr_landing_brief
    DROP COLUMN city,
    DROP COLUMN province,
    DROP COLUMN latitude,
    DROP COLUMN longitude,
    DROP COLUMN build_area,
    DROP COLUMN lot_size,
    DROP COLUMN design_budget_total,
    DROP COLUMN design_budget_min,
    DROP COLUMN design_budget_max,
    DROP COLUMN description,
    DROP COLUMN phone_number,
    DROP COLUMN start_date_type,
    DROP COLUMN expected_start_date;
