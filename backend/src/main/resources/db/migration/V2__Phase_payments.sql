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
