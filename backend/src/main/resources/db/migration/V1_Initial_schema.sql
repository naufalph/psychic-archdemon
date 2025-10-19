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
    password_hash TEXT,
    email VARCHAR(255) NOT NULL,
    social_type VARCHAR(20) NOT NULL DEFAULT 'EMAIL',
    first_nm VARCHAR(255),
    last_nm VARCHAR(255),
    is_email_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL,

    CONSTRAINT chk_email_format CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_password_not_empty CHECK (length(password_hash) > 0),
    CONSTRAINT uk_user_email_social UNIQUE (email, social_type)
);

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

-- Create email verification table
CREATE TABLE IF NOT EXISTS rmtr_email_verification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES rmtr_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '24 hours'),
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_email_verification_token ON rmtr_email_verification(token);
CREATE INDEX IF NOT EXISTS idx_email_verification_user_id ON rmtr_email_verification(user_id);

-- Create rmtr_porto table (architect portfolio)
CREATE TABLE IF NOT EXISTS rmtr_porto (
    id BIGSERIAL PRIMARY KEY,
    architect_id BIGINT NOT NULL REFERENCES rmtr_architect(id) ON DELETE CASCADE ON UPDATE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    project_date DATE,
    location VARCHAR(255),
    project_type VARCHAR(100),
    is_built BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL
);

CREATE INDEX IF NOT EXISTS idx_porto_architect_id ON rmtr_porto(architect_id);

-- Create rmtr_porto_dtl table (portfolio image details)
CREATE TABLE IF NOT EXISTS rmtr_porto_dtl (
    id BIGSERIAL PRIMARY KEY,
    porto_id BIGINT NOT NULL REFERENCES rmtr_porto(id) ON DELETE CASCADE ON UPDATE CASCADE,
    original_url TEXT NOT NULL,
    large_url TEXT NOT NULL,
    medium_url TEXT NOT NULL,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_porto_dtl_porto_id ON rmtr_porto_dtl(porto_id);