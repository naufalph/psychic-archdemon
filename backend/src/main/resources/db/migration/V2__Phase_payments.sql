CREATE TABLE rmtr_phase_payment (
    id BIGSERIAL PRIMARY KEY,
    phase_id BIGINT NOT NULL REFERENCES rmtr_bid_payment_phase(id),
    project_id BIGINT NOT NULL REFERENCES rmtr_project(id),
    client_id BIGINT NOT NULL REFERENCES rmtr_client(id),
    architect_id BIGINT NOT NULL REFERENCES rmtr_architect(id),
    amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    xendit_invoice_id VARCHAR(255),
    xendit_reference_id VARCHAR(255) UNIQUE,
    payment_link TEXT,
    payment_method VARCHAR(50),
    payment_channel VARCHAR(50),
    expires_at TIMESTAMP,
    completed_at TIMESTAMP,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_phase_payment UNIQUE (phase_id)
);

CREATE INDEX idx_phase_payment_project_id ON rmtr_phase_payment(project_id);
CREATE INDEX idx_phase_payment_xendit_ref ON rmtr_phase_payment(xendit_reference_id);

ALTER TABLE rmtr_bid_payment_phase ADD COLUMN estimated_days INTEGER NOT NULL DEFAULT 0;
ALTER TABLE rmtr_bid_payment_phase ADD CONSTRAINT chk_estimated_days_non_negative CHECK (estimated_days >= 0);

ALTER TABLE rmtr_project
    ADD COLUMN start_date_type VARCHAR(20) NOT NULL DEFAULT 'IMMEDIATELY';

ALTER TABLE rmtr_project
    ADD CONSTRAINT chk_start_date_type
        CHECK (start_date_type IN ('IMMEDIATELY', 'SPECIFIC_DATE'));

CREATE TABLE rmtr_project_phase (
                                    id           BIGSERIAL PRIMARY KEY,
                                    project_id   BIGINT NOT NULL REFERENCES rmtr_project(id),
                                    phase_number INT NOT NULL,
                                    title        VARCHAR(255) NOT NULL,
                                    description  TEXT,
                                    amount       DECIMAL(15,2) NOT NULL,
                                    status       VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                    due_date     DATE,
                                    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at   TIMESTAMP,
                                    CONSTRAINT uk_project_phase UNIQUE (project_id, phase_number)
);
CREATE INDEX idx_project_phase_project_id ON rmtr_project_phase(project_id);

CREATE TABLE rmtr_project_phase_disbursement (
                                                 id                    BIGSERIAL PRIMARY KEY,
                                                 phase_id              BIGINT NOT NULL REFERENCES rmtr_project_phase(id),
                                                 architect_id          BIGINT NOT NULL REFERENCES rmtr_architect(id),
                                                 xendit_payout_id      VARCHAR(255) UNIQUE,
                                                 xendit_reference_id   VARCHAR(255) UNIQUE,
                                                 channel_code          VARCHAR(50),
                                                 account_number        VARCHAR(255),
                                                 account_holder_name   VARCHAR(255),
                                                 amount                DECIMAL(15,2) NOT NULL,
                                                 status                VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                                 failure_code          VARCHAR(100),
                                                 xendit_raw_payload    JSONB,
                                                 initiated_at          TIMESTAMP,
                                                 completed_at          TIMESTAMP
);
CREATE INDEX idx_phase_disbursement_phase_id ON rmtr_project_phase_disbursement(phase_id);

CREATE TABLE rmtr_project_phase_deliverable (
                                                id           BIGSERIAL PRIMARY KEY,
                                                phase_id     BIGINT NOT NULL REFERENCES rmtr_project_phase(id),
                                                uploaded_by  BIGINT NOT NULL REFERENCES rmtr_user(id),
                                                file_path    VARCHAR(500) NOT NULL,
                                                file_type    VARCHAR(100),
                                                description  TEXT,
                                                uploaded_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_phase_deliverable_phase_id ON rmtr_project_phase_deliverable(phase_id);

CREATE TABLE rmtr_project_phase_log (
                                        id           BIGSERIAL PRIMARY KEY,
                                        phase_id     BIGINT NOT NULL REFERENCES rmtr_project_phase(id),
                                        actor_id     BIGINT REFERENCES rmtr_user(id),
                                        actor_type   VARCHAR(50) NOT NULL,
                                        action       VARCHAR(100) NOT NULL,
                                        from_status  VARCHAR(50),
                                        to_status    VARCHAR(50),
                                        metadata     JSONB,
                                        created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_phase_log_phase_id ON rmtr_project_phase_log(phase_id);

ALTER TABLE rmtr_phase_payment
    ADD COLUMN project_phase_id BIGINT REFERENCES rmtr_project_phase(id);
CREATE INDEX idx_phase_payment_project_phase_id ON rmtr_phase_payment(project_phase_id);
