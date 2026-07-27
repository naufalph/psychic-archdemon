CREATE TABLE rmtr_legal_document (
    id BIGSERIAL PRIMARY KEY,
    doc_type VARCHAR(30) NOT NULL,
    lang VARCHAR(5) NOT NULL,
    version VARCHAR(20) NOT NULL,
    content_md TEXT NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    effective_at TIMESTAMP,
    is_current BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT chk_legal_doc_type CHECK (doc_type IN ('ACCOUNT_TC', 'PRIVACY_POLICY')),
    CONSTRAINT chk_legal_doc_lang CHECK (lang IN ('en', 'id')),
    CONSTRAINT uq_legal_doc_type_lang_version UNIQUE (doc_type, lang, version)
);

-- Only one current version per (doc_type, lang) at a time.
CREATE UNIQUE INDEX uq_legal_doc_current
    ON rmtr_legal_document (doc_type, lang)
    WHERE is_current = true;

CREATE INDEX IF NOT EXISTS idx_legal_doc_current_lookup
    ON rmtr_legal_document (doc_type, lang, is_current);

CREATE TABLE rmtr_user_agreement_acceptance (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES rmtr_user(id),
    doc_type VARCHAR(30) NOT NULL,
    version VARCHAR(20) NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    lang VARCHAR(5) NOT NULL,
    accepted_at TIMESTAMP NOT NULL DEFAULT now(),
    ip_address VARCHAR(45),
    user_agent TEXT,

    CONSTRAINT chk_acceptance_doc_type CHECK (doc_type IN ('ACCOUNT_TC', 'PRIVACY_POLICY')),
    CONSTRAINT chk_acceptance_lang CHECK (lang IN ('en', 'id'))
);

CREATE INDEX IF NOT EXISTS idx_acceptance_user_doctype
    ON rmtr_user_agreement_acceptance (user_id, doc_type);
