ALTER TABLE rmtr_architect
    ADD COLUMN fullname_ktp VARCHAR(255),
    ADD COLUMN is_phone_verified BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE rmtr_otp_verification (
    id BIGSERIAL PRIMARY KEY,
    architect_id BIGINT NOT NULL REFERENCES rmtr_architect(id) ON DELETE CASCADE,
    phone_number VARCHAR(20) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_otp_architect_phone ON rmtr_otp_verification(architect_id, phone_number);
