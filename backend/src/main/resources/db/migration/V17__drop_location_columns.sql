-- `location` was a free-text copy of "city, province" from before structured addresses.
-- Province (a regions.js code) and city (an item from that province's list) replace it.
ALTER TABLE rmtr_project DROP COLUMN location;
ALTER TABLE rmtr_landing_brief DROP COLUMN location;
