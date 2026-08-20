-- phone_num was VARCHAR(16), but UpdateClientProfileRequest accepts ^\+?[0-9\s-]{10,16}$,
-- which permits 17 characters ('+' plus 16). A formatted Indonesian number such as
-- '+62 812 3456 7890' passed validation and then overflowed the column, surfacing as a 500.
ALTER TABLE rmtr_client ALTER COLUMN phone_num TYPE VARCHAR(24);

ALTER TABLE rmtr_architect ALTER COLUMN phone_num TYPE VARCHAR(24);
