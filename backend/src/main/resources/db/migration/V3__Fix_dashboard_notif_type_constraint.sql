-- chk_dashboard_notif_type was never updated when BIDDING_DEADLINE_REMINDER,
-- PROJECT_CLOSED_NO_WINNER, and REVISION_REQUESTED were added to NotificationType,
-- so any insert using those types (e.g. requesting a phase revision) violates the
-- constraint and rolls back the whole transaction (NotificationEventListener runs
-- BEFORE_COMMIT).
ALTER TABLE rmtr_dashboard_notif DROP CONSTRAINT chk_dashboard_notif_type;

ALTER TABLE rmtr_dashboard_notif
    ADD CONSTRAINT chk_dashboard_notif_type CHECK (type IN (
        'PROJECT_VALIDATED', 'PROJECT_UPDATED', 'BID_RECEIVED', 'BID_ACCEPTED',
        'PAYMENT_RECEIVED', 'SUPPORT_REQUESTED', 'BIDDING_DEADLINE_REMINDER',
        'PROJECT_CLOSED_NO_WINNER', 'REVISION_REQUESTED'
    ));

ALTER TABLE rmtr_phase_payment ALTER COLUMN phase_id DROP NOT NULL;

ALTER TABLE rmtr_porto ADD COLUMN made_with_rumantra BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE rmtr_porto ADD COLUMN source_project_id BIGINT;

CREATE UNIQUE INDEX uq_porto_source_project ON rmtr_porto (source_project_id) WHERE source_project_id IS NOT NULL;
