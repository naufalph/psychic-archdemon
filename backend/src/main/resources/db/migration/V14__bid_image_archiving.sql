-- Storage retention for bid images: the blob is deleted from object storage once a
-- bid is long dead, but the row is kept so bid history stays auditable.
ALTER TABLE rmtr_bid_image ADD COLUMN archived_at TIMESTAMP NULL;

CREATE INDEX idx_bid_image_archived_at ON rmtr_bid_image(archived_at);
