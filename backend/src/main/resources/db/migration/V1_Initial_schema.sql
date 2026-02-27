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
    last_login_role VARCHAR(20) NULL,

    CONSTRAINT chk_email_format CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_password_not_empty CHECK (length(password_hash) > 0),
    CONSTRAINT chk_last_login_role CHECK (last_login_role IS NULL OR last_login_role IN ('ARCHITECT', 'CLIENT')),
    CONSTRAINT uk_user_email_social UNIQUE (email, social_type)
);

CREATE INDEX idx_user_last_login_role ON rmtr_user(last_login_role);

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
    city VARCHAR(255),
    experience_range VARCHAR(50),
    philosophy TEXT,
    expertise JSONB,
    success_project INT DEFAULT 0 CHECK (success_project >= 0),
    needs_onboarding BOOLEAN DEFAULT TRUE,
    onboarding_completed_at TIMESTAMP
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
    title VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    budget_total BIGINT,
    design_budget_min BIGINT CHECK (design_budget_min >= 0),
    design_budget_max BIGINT CHECK (design_budget_max >= design_budget_min),
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
    bidding_deadline TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT chk_project_status CHECK (status IN ('PENDING_APPROVAL', 'REJECTED', 'OPEN', 'BIDDING_CLOSED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_project_client_id ON rmtr_project(client_id);
CREATE INDEX IF NOT EXISTS idx_project_created_at ON rmtr_project(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_project_status ON rmtr_project(status);
CREATE INDEX IF NOT EXISTS idx_project_bidding_deadline ON rmtr_project(bidding_deadline) WHERE bidding_deadline IS NOT NULL;

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
    message_code VARCHAR(100),
    message_data TEXT,
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

CREATE TABLE IF NOT EXISTS rmtr_token_purchase (
    id BIGSERIAL PRIMARY KEY,
    architect_id BIGINT NOT NULL REFERENCES rmtr_architect(id) ON DELETE CASCADE ON UPDATE CASCADE,
    quantity INTEGER NOT NULL CHECK (quantity >= 1 AND quantity <= 50),
    price_per_token DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,
    tier VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    xendit_payment_request_id VARCHAR(255),
    xendit_reference_id VARCHAR(255) NOT NULL UNIQUE,
    payment_link TEXT,
    payment_method VARCHAR(50),
    payment_channel VARCHAR(50),
    expires_at TIMESTAMP,
    completed_at TIMESTAMP,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL

    CONSTRAINT chk_token_purchase_tier CHECK (tier IN ('FREE', 'BASIC')),
    CONSTRAINT chk_token_purchase_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'EXPIRED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_token_purchase_architect_id ON rmtr_token_purchase(architect_id);
CREATE INDEX IF NOT EXISTS idx_token_purchase_status ON rmtr_token_purchase(status);
CREATE INDEX IF NOT EXISTS idx_token_purchase_xendit_ref ON rmtr_token_purchase(xendit_reference_id);
CREATE INDEX IF NOT EXISTS idx_token_purchase_created_at ON rmtr_token_purchase(created_at DESC);

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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    deliverables JSONB,
    site_analysis_revisions INT,
    design_revisions INT,
    permits_doc_revisions INT,
    specialized_services_revisions INT,
    construction_support_revisions INT
);

CREATE INDEX IF NOT EXISTS idx_bid_detail_bid_id ON rmtr_bid_detail(bid_id);
CREATE INDEX idx_bid_detail_deliverables ON rmtr_bid_detail USING GIN (deliverables);

CREATE TABLE IF NOT EXISTS rmtr_bid_image (
    id BIGSERIAL PRIMARY KEY,
    bid_id BIGINT NOT NULL REFERENCES rmtr_bid(id) ON DELETE CASCADE,
    image_type VARCHAR(30) NOT NULL,
    image_url TEXT NOT NULL,
    display_order INTEGER DEFAULT 0,
    file_name VARCHAR(255),
    file_size BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_bid_image_type CHECK (image_type IN ('FACADE', 'INTERIOR', 'MASSING', 'ZONING'))
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

    CONSTRAINT chk_bid_usage_action CHECK (action IN ('BID_PLACED', 'BID_REFUNDED', 'TOKEN_ALLOCATED', 'TOKEN_PURCHASED', 'QUOTA_UPGRADED', 'QUOTA_DOWNGRADED', 'MANUAL_ADJUSTMENT'))
);

CREATE INDEX IF NOT EXISTS idx_bid_usage_log_architect_id ON rmtr_bid_usage_log(architect_id);
CREATE INDEX IF NOT EXISTS idx_bid_usage_log_bid_id ON rmtr_bid_usage_log(bid_id);
CREATE INDEX IF NOT EXISTS idx_bid_usage_log_timestamp ON rmtr_bid_usage_log(timestamp DESC);

CREATE TABLE rmtr_conversation (
                                   id BIGSERIAL PRIMARY KEY,
                                   project_id BIGINT NOT NULL REFERENCES rmtr_project(id) ON DELETE CASCADE,
                                   bid_id BIGINT NOT NULL UNIQUE REFERENCES rmtr_bid(id) ON DELETE CASCADE,
                                   architect_id BIGINT NOT NULL REFERENCES rmtr_architect(id),
                                   client_id BIGINT NOT NULL REFERENCES rmtr_client(id),
                                   status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP,
                                   last_message_at TIMESTAMP,

                                   CONSTRAINT uk_conversation_bid UNIQUE(bid_id),
                                   CONSTRAINT uk_conversation_project_architect UNIQUE(project_id, architect_id),
                                   CONSTRAINT chk_conversation_status CHECK (status IN ('ACTIVE', 'ARCHIVED', 'CLOSED'))
);

CREATE INDEX idx_conversation_architect ON rmtr_conversation(architect_id);
CREATE INDEX idx_conversation_client ON rmtr_conversation(client_id);
CREATE INDEX idx_conversation_project ON rmtr_conversation(project_id);

CREATE TABLE rmtr_message (
                              id BIGSERIAL PRIMARY KEY,
                              conversation_id BIGINT NOT NULL REFERENCES rmtr_conversation(id) ON DELETE CASCADE,
                              sender_user_id BIGINT NOT NULL REFERENCES rmtr_user(id),
                              sender_type VARCHAR(20) NOT NULL,
                              content TEXT NOT NULL,
                              message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
                              is_read BOOLEAN DEFAULT FALSE,
                              read_at TIMESTAMP,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP,

                              CONSTRAINT chk_sender_type CHECK (sender_type IN ('ARCHITECT', 'CLIENT')),
                              CONSTRAINT chk_message_type CHECK (message_type IN ('TEXT', 'FILE', 'IMAGE'))
);

CREATE INDEX idx_message_conversation ON rmtr_message(conversation_id);
CREATE INDEX idx_message_sender ON rmtr_message(sender_user_id);
CREATE INDEX idx_message_created ON rmtr_message(conversation_id, created_at DESC);
CREATE INDEX idx_message_unread ON rmtr_message(conversation_id, is_read) WHERE is_read = FALSE;

CREATE TABLE rmtr_message_file (
                                   id BIGSERIAL PRIMARY KEY,
                                   message_id BIGINT NOT NULL REFERENCES rmtr_message(id) ON DELETE CASCADE,
                                   file_name VARCHAR(255) NOT NULL,
                                   file_url TEXT NOT NULL,
                                   file_type VARCHAR(50),
                                   file_size BIGINT,
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT uk_message_file UNIQUE(message_id)
);

CREATE INDEX idx_message_file_message ON rmtr_message_file(message_id);

-- Add PDF attachment support for bids
CREATE TABLE IF NOT EXISTS rmtr_bid_attachment (
                                                   id BIGSERIAL PRIMARY KEY,
                                                   bid_id BIGINT NOT NULL REFERENCES rmtr_bid(id) ON DELETE CASCADE,
    file_url TEXT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(20) NOT NULL,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_attachment_file_type CHECK (file_type IN ('PDF', 'DOCUMENT'))
    );

CREATE INDEX IF NOT EXISTS idx_bid_attachment_bid_id ON rmtr_bid_attachment(bid_id);
