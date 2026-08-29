-- The client form has always collected the lot size ("Luas Lahan") but stored it in
-- estimated_build_area for want of a dedicated column, which is why every architect-facing
-- view had to be relabelled to "Luas Lahan" in rmtr46. Give lot size its own column, move the
-- existing values across, and free estimated_build_area to finally mean the building area.
ALTER TABLE rmtr_project ADD COLUMN lot_size INTEGER CHECK (lot_size > 0);

UPDATE rmtr_project SET lot_size = estimated_build_area WHERE estimated_build_area IS NOT NULL;

-- Build area was never actually collected, so no existing row has a real value for it.
UPDATE rmtr_project SET estimated_build_area = NULL;
