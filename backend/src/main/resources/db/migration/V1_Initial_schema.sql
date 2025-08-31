-- Create rmtr_architect table
CREATE TABLE IF NOT EXISTS rmtr_architect (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES rmtr_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    company_name VARCHAR(255),
    company_site VARCHAR(255),
    contact_name VARCHAR(255),
    category VARCHAR(10),
    phone_num VARCHAR(16),
    ktp_num VARCHAR(16),
    is_ktp_verified BOOLEAN DEFAULT FALSE,
    npwp VARCHAR(16),
    is_npwp_verified BOOLEAN DEFAULT FALSE,
    bid_left INT CHECK (bid_left >= 0),
    success_match INT DEFAULT 0 CHECK (success_match >= 0),
    success_project INT DEFAULT 0 CHECK (success_project >= 0)
);

-- Create rmtr_user table based on JPA entity
CREATE TABLE IF NOT EXISTS rmtr_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_nm VARCHAR(255),
    last_nm VARCHAR(255),
    is_email_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL,

    CONSTRAINT chk_username_not_empty CHECK (username IS NOT NULL AND trim(username) != ''),
    CONSTRAINT chk_email_format CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_password_not_empty CHECK (length(password_hash) > 0)
);

CREATE INDEX IF NOT EXISTS idx_user_username ON rmtr_user(username);
CREATE INDEX IF NOT EXISTS idx_user_email ON rmtr_user(email);

CREATE TABLE IF NOT EXISTS rmtr_client (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL unique REFERENCES rmtr_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    phone_num VARCHAR(16),
    is_phonenum_verified BOOLEAN DEFAULT FALSE,
    ktp_num VARCHAR(16),
    is_ktp_verified BOOLEAN DEFAULT FALSE,
    project_match INTEGER DEFAULT 0,
    project_finished INTEGER DEFAULT 0
);