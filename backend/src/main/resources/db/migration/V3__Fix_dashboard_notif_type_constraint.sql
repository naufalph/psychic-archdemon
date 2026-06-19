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
