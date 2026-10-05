-- Notifications carry the project they refer to, so the frontend can deep-link without
-- resolving BID/PHASE references itself. Nullable: support/token/subscription notices have none.
ALTER TABLE rmtr_dashboard_notif ADD COLUMN project_id BIGINT;

UPDATE rmtr_dashboard_notif n
SET project_id = p.id
FROM rmtr_project p
WHERE n.reference_type = 'PROJECT' AND p.id = n.reference_id;

UPDATE rmtr_dashboard_notif n
SET project_id = b.project_id
FROM rmtr_bid b
WHERE n.reference_type = 'BID' AND b.id = n.reference_id;

UPDATE rmtr_dashboard_notif n
SET project_id = ph.project_id
FROM rmtr_project_phase ph
WHERE n.reference_type = 'PHASE' AND ph.id = n.reference_id;

ALTER TABLE rmtr_dashboard_notif
    ADD CONSTRAINT fk_dashboard_notif_project FOREIGN KEY (project_id) REFERENCES rmtr_project (id);

CREATE INDEX idx_dashboard_notif_project_id ON rmtr_dashboard_notif (project_id);
