-- Keyset pagination for the notifications page orders by (created_at DESC, id DESC) per user.
CREATE INDEX idx_dashboard_notif_user_created
    ON rmtr_dashboard_notif (user_id, created_at DESC, id DESC);

CREATE INDEX idx_dashboard_notif_user_unread
    ON rmtr_dashboard_notif (user_id, created_at DESC, id DESC)
    WHERE is_read = false;
