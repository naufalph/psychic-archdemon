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
    is_superuser BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL,

    CONSTRAINT chk_email_format CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_password_not_empty CHECK (length(password_hash) > 0),
    CONSTRAINT uk_user_email_social UNIQUE (email, social_type)
);

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
    success_match INT DEFAULT 0 CHECK (success_match >= 0),
    success_project INT DEFAULT 0 CHECK (success_project >= 0)
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

-- Create rmtr_project table (client projects)
CREATE TABLE IF NOT EXISTS rmtr_project (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES rmtr_client(id) ON DELETE CASCADE ON UPDATE CASCADE,
    budget_min BIGINT NOT NULL CHECK (budget_min >= 0),
    budget_max BIGINT NOT NULL CHECK (budget_max >= budget_min),
    project_category VARCHAR(255),
    building_function VARCHAR(255),
    estimated_build_area INTEGER CHECK (estimated_build_area > 0),
    number_of_floors INTEGER CHECK (number_of_floors > 0),
    owns_land BOOLEAN,
    has_legal_documents BOOLEAN,
    scope_of_work TEXT,
    deliverables JSONB,
    design_preferences TEXT,
    contact_person VARCHAR(255),
    expected_start_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_APPROVAL',
    validation_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT chk_project_status CHECK (status IN ('PENDING_APPROVAL', 'REJECTED', 'OPEN', 'BIDDING_CLOSED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_project_client_id ON rmtr_project(client_id);
CREATE INDEX IF NOT EXISTS idx_project_budget ON rmtr_project(budget_min, budget_max);
CREATE INDEX IF NOT EXISTS idx_project_created_at ON rmtr_project(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_project_status ON rmtr_project(status);

-- Create rmtr_project_file table (project file uploads)
CREATE TABLE IF NOT EXISTS rmtr_project_file (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES rmtr_project(id) ON DELETE CASCADE ON UPDATE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_project_file_project_id ON rmtr_project_file(project_id);

-- Create rmtr_dashboard_notif table (dashboard notifications)
CREATE TABLE IF NOT EXISTS rmtr_dashboard_notif (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES rmtr_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_dashboard_notif_type CHECK (type IN ('PROJECT_VALIDATED', 'PROJECT_UPDATED', 'BID_RECEIVED', 'BID_ACCEPTED', 'PAYMENT_RECEIVED'))
);

CREATE INDEX IF NOT EXISTS idx_dashboard_notif_user_id ON rmtr_dashboard_notif(user_id);
CREATE INDEX IF NOT EXISTS idx_dashboard_notif_user_read ON rmtr_dashboard_notif(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_dashboard_notif_created_at ON rmtr_dashboard_notif(created_at DESC);

-- Create rmtr_bid_quota table (tracks bid token balance for architects)
CREATE TABLE IF NOT EXISTS rmtr_bid_quota (
    id BIGSERIAL PRIMARY KEY,
    architect_id BIGINT NOT NULL UNIQUE REFERENCES rmtr_architect(id) ON DELETE CASCADE ON UPDATE CASCADE,
    tier VARCHAR(20) NOT NULL DEFAULT 'FREE',
    tokens_allocated INTEGER NOT NULL DEFAULT 0 CHECK (tokens_allocated >= 0),
    tokens_remaining INTEGER NOT NULL DEFAULT 0 CHECK (tokens_remaining >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL

    CONSTRAINT chk_bid_quota_tier CHECK (tier IN ('FREE', 'BASIC'))
);

CREATE INDEX IF NOT EXISTS idx_bid_quota_architect_id ON rmtr_bid_quota(architect_id);

-- Create rmtr_subscription table (tracks subscription history for architects)
CREATE TABLE IF NOT EXISTS rmtr_subscription (
    id BIGSERIAL PRIMARY KEY,
    architect_id BIGINT NOT NULL REFERENCES rmtr_architect(id) ON DELETE CASCADE ON UPDATE CASCADE,
    tier VARCHAR(20) NOT NULL DEFAULT 'FREE',
    start_date DATE NOT NULL,
    end_date DATE NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    yearly_price DECIMAL(10, 2) NULL,
    xendit_plan_id VARCHAR(255) NULL,
    xendit_reference_id VARCHAR(255) NULL,
    xendit_cycle_id VARCHAR(255) NULL,
    payment_link TEXT NULL,
    last_payment_date TIMESTAMP NULL,
    next_billing_date DATE NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL

    CONSTRAINT chk_subscription_tier CHECK (tier IN ('FREE', 'BASIC')),
    CONSTRAINT chk_subscription_status CHECK (status IN ('ACTIVE', 'PENDING', 'EXPIRED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_subscription_architect_id ON rmtr_subscription(architect_id);
CREATE INDEX IF NOT EXISTS idx_subscription_active ON rmtr_subscription(architect_id, is_active);
CREATE INDEX IF NOT EXISTS idx_subscription_xendit_ref ON rmtr_subscription(xendit_reference_id);
CREATE INDEX IF NOT EXISTS idx_subscription_xendit_cycle ON rmtr_subscription(xendit_cycle_id);

-- Create rmtr_bid table (architect bids on projects)
CREATE TABLE IF NOT EXISTS rmtr_bid (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES rmtr_project(id) ON DELETE CASCADE ON UPDATE CASCADE,
    architect_id BIGINT NOT NULL REFERENCES rmtr_architect(id) ON DELETE CASCADE ON UPDATE CASCADE,
    bid_amount DECIMAL(15, 2) NOT NULL CHECK (bid_amount > 0),
    proposed_timeline_days INTEGER CHECK (proposed_timeline_days > 0),
    proposal TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL,
    submitted_at TIMESTAMP NULL,
    accepted_at TIMESTAMP NULL,
    rejected_at TIMESTAMP NULL,
    CONSTRAINT chk_bid_status CHECK (status IN ('DRAFT', 'PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN', 'REFUNDED')),
    CONSTRAINT uk_bid_project_architect UNIQUE (project_id, architect_id)
);

CREATE INDEX IF NOT EXISTS idx_bid_project_id ON rmtr_bid(project_id);
CREATE INDEX IF NOT EXISTS idx_bid_architect_id ON rmtr_bid(architect_id);
CREATE INDEX IF NOT EXISTS idx_bid_status ON rmtr_bid(status);

CREATE TABLE IF NOT EXISTS rmtr_bid_detail (
    id BIGSERIAL PRIMARY KEY,
    bid_id BIGINT NOT NULL UNIQUE REFERENCES rmtr_bid(id) ON DELETE CASCADE,
    concept_statement TEXT,
    project_risks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_bid_detail_bid_id ON rmtr_bid_detail(bid_id);

CREATE TABLE IF NOT EXISTS rmtr_bid_image (
    id BIGSERIAL PRIMARY KEY,
    bid_id BIGINT NOT NULL REFERENCES rmtr_bid(id) ON DELETE CASCADE,
    image_type VARCHAR(30) NOT NULL,
    image_url TEXT NOT NULL,
    display_order INTEGER DEFAULT 0,
    file_name VARCHAR(255),
    file_size BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_bid_image_type CHECK (image_type IN ('CONCEPT_SKETCH', 'MOOD_BOARD'))
);

CREATE INDEX IF NOT EXISTS idx_bid_image_bid_id ON rmtr_bid_image(bid_id);
CREATE INDEX IF NOT EXISTS idx_bid_image_type ON rmtr_bid_image(bid_id, image_type);

CREATE TABLE IF NOT EXISTS rmtr_bid_portfolio_ref (
    id BIGSERIAL PRIMARY KEY,
    bid_id BIGINT NOT NULL REFERENCES rmtr_bid(id) ON DELETE CASCADE,
    porto_id BIGINT NOT NULL REFERENCES rmtr_porto(id) ON DELETE CASCADE,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_bid_portfolio UNIQUE (bid_id, porto_id)
);

CREATE INDEX IF NOT EXISTS idx_bid_portfolio_ref_bid_id ON rmtr_bid_portfolio_ref(bid_id);
CREATE INDEX IF NOT EXISTS idx_bid_portfolio_ref_porto_id ON rmtr_bid_portfolio_ref(porto_id);

-- Create rmtr_bid_usage_log table (audit trail for bid quota changes)
CREATE TABLE IF NOT EXISTS rmtr_bid_usage_log (
    id BIGSERIAL PRIMARY KEY,
    architect_id BIGINT NOT NULL REFERENCES rmtr_architect(id) ON DELETE CASCADE ON UPDATE CASCADE,
    bid_id BIGINT NULL REFERENCES rmtr_bid(id) ON DELETE SET NULL ON UPDATE CASCADE,
    action VARCHAR(30) NOT NULL,
    quota_change INTEGER NOT NULL,
    quota_after INTEGER NOT NULL CHECK (quota_after >= 0),
    description TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_bid_usage_action CHECK (action IN ('BID_PLACED', 'BID_REFUNDED', 'TOKEN_ALLOCATED', 'QUOTA_UPGRADED', 'QUOTA_DOWNGRADED', 'MANUAL_ADJUSTMENT'))
);

CREATE INDEX IF NOT EXISTS idx_bid_usage_log_architect_id ON rmtr_bid_usage_log(architect_id);
CREATE INDEX IF NOT EXISTS idx_bid_usage_log_bid_id ON rmtr_bid_usage_log(bid_id);
CREATE INDEX IF NOT EXISTS idx_bid_usage_log_timestamp ON rmtr_bid_usage_log(timestamp DESC);