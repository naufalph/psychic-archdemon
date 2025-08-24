-- Create rmtr_architect table based on JPA entity
CREATE TABLE IF NOT EXISTS rmtr_architect (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    company_name VARCHAR(255) NOT NULL,
    company_site VARCHAR(255),
    contact_name VARCHAR(255) NOT NULL,
    ktp_num VARCHAR(16) NOT NULL,
    is_ktp_verified BOOLEAN DEFAULT FALSE,
    npwp VARCHAR(16) NOT NULL,
    is_npwp_verified BOOLEAN DEFAULT FALSE,
    bid_left INT,
    success_match INT DEFAULT 0,
    success_project INT DEFAULT 0

    -- Foreign key constraint (assuming users table exists)
--    CONSTRAINT fk_architect_user FOREIGN KEY (user_id) REFERENCES rmtr_user(id) ON DELETE CASCADE,

    -- Additional constraints for data integrity
--    CONSTRAINT chk_ktp_num_length CHECK (length(ktp_num) = 16),
--    CONSTRAINT chk_npwp_length CHECK (length(npwp) = 16),
--    CONSTRAINT chk_success_match_non_negative CHECK (success_match >= 0),
--    CONSTRAINT chk_success_project_non_negative CHECK (success_project >= 0),
--    CONSTRAINT chk_bid_left_non_negative CHECK (bid_left >= 0)
);

-- Create indexes for better performance
--CREATE INDEX IF NOT EXISTS idx_architect_user_id ON rmtr_architect(user_id);
--CREATE INDEX IF NOT EXISTS idx_architect_company_name ON rmtr_architect(company_name);
--CREATE INDEX IF NOT EXISTS idx_architect_ktp_num ON rmtr_architect(ktp_num);
--CREATE INDEX IF NOT EXISTS idx_architect_npwp ON rmtr_architect(npwp);

-- Create rmtr_user table based on JPA entity
CREATE TABLE IF NOT EXISTS rmtr_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    is_email_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL

    -- Additional constraints for data integrity
--    CONSTRAINT chk_username_not_empty CHECK (length(trim(username)) > 0),
--    CONSTRAINT chk_email_format CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
--    CONSTRAINT chk_password_not_empty CHECK (length(password_hash) > 0)
);

-- Create indexes for better performance
--CREATE INDEX IF NOT EXISTS idx_user_username ON rmtr_user(username);
--CREATE INDEX IF NOT EXISTS idx_user_email ON rmtr_user(email);
--CREATE INDEX IF NOT EXISTS idx_user_active ON rmtr_user(is_active);
--CREATE INDEX IF NOT EXISTS idx_user_email_verified ON rmtr_user(is_email_verified);
--CREATE INDEX IF NOT EXISTS idx_user_created_at ON rmtr_user(created_at);
