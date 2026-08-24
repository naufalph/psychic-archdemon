ALTER TABLE rmtr_project DROP COLUMN budget_total;

ALTER TABLE rmtr_bid_detail
    ADD COLUMN facade_description TEXT,
    ADD COLUMN interior_description TEXT,
    ADD COLUMN massing_description TEXT,
    ADD COLUMN zoning_description TEXT;

ALTER TABLE rmtr_dashboard_notif DROP CONSTRAINT chk_dashboard_notif_type;

ALTER TABLE rmtr_dashboard_notif
    ADD CONSTRAINT chk_dashboard_notif_type CHECK (type IN (
        'PROJECT_VALIDATED', 'PROJECT_UPDATED', 'BID_RECEIVED', 'BID_ACCEPTED', 'BID_REJECTED',
        'PAYMENT_RECEIVED', 'SUPPORT_REQUESTED', 'BIDDING_DEADLINE_REMINDER',
        'PROJECT_CLOSED_NO_WINNER', 'REVISION_REQUESTED', 'NEGOTIATION_DEADLINE_REMINDER',
        'NEGOTIATION_EXPIRED', 'NEGOTIATION_DISPUTE_RESOLVED'
    ));

ALTER TABLE rmtr_architect ADD COLUMN full_address TEXT;
ALTER TABLE rmtr_architect ADD COLUMN province VARCHAR(100);
ALTER TABLE rmtr_architect ADD COLUMN photo_url VARCHAR(500);

ALTER TABLE rmtr_architect DROP COLUMN needs_onboarding;
ALTER TABLE rmtr_architect DROP COLUMN onboarding_completed_at;
