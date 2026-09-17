-- Squashed Aug-2026 beta release migration.
--
-- This one file is the concatenation, in order, of what were nine separate migrations during the
-- dev-bugfix-Aug2026 branch's development (originally V11 through V19). Beta was at V10 when this
-- was written, so it had never applied any of them; collapsing them keeps one deploy at one Flyway
-- version, and gives the whole release a single transaction to roll back if any statement fails.
--
-- Section order is load-bearing:
--   * the status ledger section must precede the project contract section -- the contract's
--     append-only triggers call rmtr_reject_mutation(), created by the ledger section;
--   * the two UPDATEs in the lot size section must stay adjacent and in order -- the first copies
--     estimated_build_area into lot_size, the second nulls the source;
--   * the IAI taxonomy section is data-only and runs last.


-- ===== from V11__project_site_location.sql: structured site location on the project =====

ALTER TABLE rmtr_project
    ADD COLUMN full_address TEXT,
    ADD COLUMN city         VARCHAR(255),
    ADD COLUMN province     VARCHAR(100),
    ADD COLUMN latitude     NUMERIC(10,7),
    ADD COLUMN longitude    NUMERIC(10,7);


-- ===== from V12__project_lot_size_and_build_area.sql: split lot size out of estimated build area =====

-- The client form has always collected the lot size ("Luas Lahan") but stored it in
-- estimated_build_area for want of a dedicated column, which is why every architect-facing
-- view had to be relabelled to "Luas Lahan" in rmtr46. Give lot size its own column, move the
-- existing values across, and free estimated_build_area to finally mean the building area.
ALTER TABLE rmtr_project ADD COLUMN lot_size INTEGER CHECK (lot_size > 0);

UPDATE rmtr_project SET lot_size = estimated_build_area WHERE estimated_build_area IS NOT NULL;

-- Build area was never actually collected, so no existing row has a real value for it.
UPDATE rmtr_project SET estimated_build_area = NULL;


-- ===== from V13__architect_education_and_university.sql: architect education history and the university reference list =====

-- Architect education history (repeatable list: degree level, university, field of study, year).
-- Stored the same way as rmtr_architect.expertise (a JSONB list on the row) since education
-- entries are small structured objects owned entirely by the architect, not shared/queried
-- independently — a child table would add join overhead with no benefit here.
ALTER TABLE rmtr_architect ADD COLUMN education JSONB;

-- Reference list of universities offering an architecture program, used to back the
-- searchable dropdown in the architect profile education form. Indonesian universities are
-- sourced from BAN-PT "Unggul"/A-accredited program listings; international universities are
-- the QS World University Rankings by Subject 2026 (Architecture & Built Environment) top 100.
-- qs_rank is NULL for Indonesian entries (QS ranks architecture programs globally, not by
-- country) and approximate (tied band start) for QS rank 51-100 since QS does not publish an
-- exact order within that band.
CREATE TABLE rmtr_university (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    country VARCHAR(100) NOT NULL,
    city VARCHAR(150),
    is_indonesia BOOLEAN NOT NULL DEFAULT FALSE,
    qs_rank INT,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_university_name ON rmtr_university (LOWER(name));
CREATE INDEX idx_university_is_indonesia ON rmtr_university (is_indonesia, sort_order);

-- Indonesian universities with an architecture program (S1, many also with S2/S3)
INSERT INTO rmtr_university (name, country, city, is_indonesia, qs_rank, sort_order) VALUES
('Institut Teknologi Bandung', 'Indonesia', 'Bandung', TRUE, NULL, 1),
('Universitas Indonesia', 'Indonesia', 'Depok', TRUE, NULL, 2),
('Universitas Gadjah Mada', 'Indonesia', 'Yogyakarta', TRUE, NULL, 3),
('Institut Teknologi Sepuluh Nopember', 'Indonesia', 'Surabaya', TRUE, NULL, 4),
('Universitas Diponegoro', 'Indonesia', 'Semarang', TRUE, NULL, 5),
('Universitas Katolik Parahyangan', 'Indonesia', 'Bandung', TRUE, NULL, 6),
('Universitas Islam Indonesia', 'Indonesia', 'Yogyakarta', TRUE, NULL, 7),
('Universitas Trisakti', 'Indonesia', 'Jakarta', TRUE, NULL, 8),
('Universitas Brawijaya', 'Indonesia', 'Malang', TRUE, NULL, 9),
('Universitas Hasanuddin', 'Indonesia', 'Makassar', TRUE, NULL, 10),
('Universitas Sebelas Maret', 'Indonesia', 'Surakarta', TRUE, NULL, 11),
('Universitas Warmadewa', 'Indonesia', 'Denpasar', TRUE, NULL, 12),
('Universitas Kristen Petra', 'Indonesia', 'Surabaya', TRUE, NULL, 13),
('Universitas Bina Nusantara', 'Indonesia', 'Jakarta', TRUE, NULL, 14),
('Universitas Kristen Duta Wacana', 'Indonesia', 'Yogyakarta', TRUE, NULL, 15),
('Universitas Udayana', 'Indonesia', 'Denpasar', TRUE, NULL, 16),
('Universitas Katolik Soegijapranata', 'Indonesia', 'Semarang', TRUE, NULL, 17),
('Universitas Pancasila', 'Indonesia', 'Jakarta', TRUE, NULL, 18),
('Universitas Muhammadiyah Makassar', 'Indonesia', 'Makassar', TRUE, NULL, 19),
('Universitas Sumatera Utara', 'Indonesia', 'Medan', TRUE, NULL, 20),
('Universitas Kristen Indonesia', 'Indonesia', 'Jakarta', TRUE, NULL, 21),
('Universitas Gunadarma', 'Indonesia', 'Depok', TRUE, NULL, 22),
('Universitas Muhammadiyah Surakarta', 'Indonesia', 'Surakarta', TRUE, NULL, 23),
('Universitas Sam Ratulangi', 'Indonesia', 'Manado', TRUE, NULL, 24),
('Universitas Tarumanagara', 'Indonesia', 'Jakarta', TRUE, NULL, 25),
('Universitas Atma Jaya Yogyakarta', 'Indonesia', 'Yogyakarta', TRUE, NULL, 26),
('Universitas Pelita Harapan', 'Indonesia', 'Tangerang', TRUE, NULL, 27),
('Institut Teknologi Nasional Bandung', 'Indonesia', 'Bandung', TRUE, NULL, 28),
('Universitas Andalas', 'Indonesia', 'Padang', TRUE, NULL, 29),
('Universitas Riau', 'Indonesia', 'Pekanbaru', TRUE, NULL, 30),
('Universitas Lampung', 'Indonesia', 'Bandar Lampung', TRUE, NULL, 31),
('Universitas Mercu Buana', 'Indonesia', 'Jakarta', TRUE, NULL, 32),
('Universitas Esa Unggul', 'Indonesia', 'Jakarta', TRUE, NULL, 33),
('Universitas Tanjungpura', 'Indonesia', 'Pontianak', TRUE, NULL, 34),
('Universitas Sriwijaya', 'Indonesia', 'Palembang', TRUE, NULL, 35),
('Universitas Syiah Kuala', 'Indonesia', 'Banda Aceh', TRUE, NULL, 36),
('Institut Teknologi Sumatera', 'Indonesia', 'Lampung Selatan', TRUE, NULL, 37),
('Universitas Sultan Ageng Tirtayasa', 'Indonesia', 'Serang', TRUE, NULL, 38),
('Universitas Jember', 'Indonesia', 'Jember', TRUE, NULL, 39),
('Universitas Muhammadiyah Jakarta', 'Indonesia', 'Jakarta', TRUE, NULL, 40),
('Universitas Bung Hatta', 'Indonesia', 'Padang', TRUE, NULL, 41),
('Universitas Pendidikan Indonesia', 'Indonesia', 'Bandung', TRUE, NULL, 42),
('Universitas 17 Agustus 1945 Surabaya', 'Indonesia', 'Surabaya', TRUE, NULL, 43),
('Universitas Pembangunan Nasional Veteran Jawa Timur', 'Indonesia', 'Surabaya', TRUE, NULL, 44),
('Universitas Merdeka Malang', 'Indonesia', 'Malang', TRUE, NULL, 45),
('Universitas Malikussaleh', 'Indonesia', 'Lhokseumawe', TRUE, NULL, 46),
('Universitas Muslim Indonesia', 'Indonesia', 'Makassar', TRUE, NULL, 47);

-- International universities: QS World University Rankings by Subject 2026,
-- Architecture & Built Environment, ranks 1-100
INSERT INTO rmtr_university (name, country, city, is_indonesia, qs_rank, sort_order) VALUES
('University College London', 'United Kingdom', NULL, FALSE, 1, 1001),
('Massachusetts Institute of Technology', 'United States', NULL, FALSE, 2, 1002),
('Delft University of Technology', 'Netherlands', NULL, FALSE, 3, 1003),
('ETH Zurich', 'Switzerland', NULL, FALSE, 4, 1004),
('Manchester School of Architecture', 'United Kingdom', NULL, FALSE, 5, 1005),
('Politecnico di Milano', 'Italy', NULL, FALSE, 6, 1006),
('Harvard University', 'United States', NULL, FALSE, 7, 1007),
('National University of Singapore', 'Singapore', NULL, FALSE, 7, 1008),
('Tsinghua University', 'China', NULL, FALSE, 9, 1009),
('University of California, Berkeley', 'United States', NULL, FALSE, 10, 1010),
('École Polytechnique Fédérale de Lausanne', 'Switzerland', NULL, FALSE, 11, 1011),
('Tongji University', 'China', NULL, FALSE, 12, 1012),
('University of Cambridge', 'United Kingdom', NULL, FALSE, 13, 1013),
('University of Hong Kong', 'Hong Kong', NULL, FALSE, 14, 1014),
('RMIT University', 'Australia', NULL, FALSE, 15, 1015),
('Columbia University', 'United States', NULL, FALSE, 16, 1016),
('The University of Tokyo', 'Japan', NULL, FALSE, 17, 1017),
('Politecnico di Torino', 'Italy', NULL, FALSE, 18, 1018),
('Cornell University', 'United States', NULL, FALSE, 19, 1019),
('Universitat Politècnica de Catalunya', 'Spain', NULL, FALSE, 19, 1020),
('Hong Kong Polytechnic University', 'Hong Kong', NULL, FALSE, 21, 1021),
('Universidad Politécnica de Madrid', 'Spain', NULL, FALSE, 21, 1022),
('The University of Melbourne', 'Australia', NULL, FALSE, 23, 1023),
('Technische Universität Berlin', 'Germany', NULL, FALSE, 24, 1024),
('Technical University of Munich', 'Germany', NULL, FALSE, 25, 1025),
('University of Sheffield', 'United Kingdom', NULL, FALSE, 26, 1026),
('Nanyang Technological University', 'Singapore', NULL, FALSE, 27, 1027),
('Georgia Institute of Technology', 'United States', NULL, FALSE, 28, 1028),
('Stanford University', 'United States', NULL, FALSE, 28, 1029),
('The University of Sydney', 'Australia', NULL, FALSE, 28, 1030),
('Seoul National University', 'South Korea', NULL, FALSE, 31, 1031),
('University of California, Los Angeles', 'United States', NULL, FALSE, 32, 1032),
('UNSW Sydney', 'Australia', NULL, FALSE, 33, 1033),
('Pontificia Universidad Católica de Chile', 'Chile', NULL, FALSE, 34, 1034),
('University of Pennsylvania', 'United States', NULL, FALSE, 34, 1035),
('Aalto University', 'Finland', NULL, FALSE, 36, 1036),
('Yale University', 'United States', NULL, FALSE, 36, 1037),
('Institute of Science Tokyo', 'Japan', NULL, FALSE, 38, 1038),
('Princeton University', 'United States', NULL, FALSE, 39, 1039),
('Tianjin University', 'China', NULL, FALSE, 40, 1040),
('Università Iuav di Venezia', 'Italy', NULL, FALSE, 40, 1041),
('Southeast University', 'China', NULL, FALSE, 42, 1042),
('University of Oxford', 'United Kingdom', NULL, FALSE, 42, 1043),
('KTH Royal Institute of Technology', 'Sweden', NULL, FALSE, 44, 1044),
('TU Wien', 'Austria', NULL, FALSE, 44, 1045),
('University of Toronto', 'Canada', NULL, FALSE, 44, 1046),
('KU Leuven', 'Belgium', NULL, FALSE, 47, 1047),
('Universidade de São Paulo', 'Brazil', NULL, FALSE, 48, 1048),
('University of Michigan', 'United States', NULL, FALSE, 49, 1049),
('Cardiff University', 'United Kingdom', NULL, FALSE, 50, 1050),
('University of Technology Malaysia', 'Malaysia', NULL, FALSE, 50, 1051),
('Adelaide University', 'Australia', NULL, FALSE, 51, 1052),
('Architectural Association School of Architecture', 'United Kingdom', NULL, FALSE, 51, 1053),
('Carnegie Mellon University', 'United States', NULL, FALSE, 51, 1054),
('Chalmers University of Technology', 'Sweden', NULL, FALSE, 51, 1055),
('Eindhoven University of Technology', 'Netherlands', NULL, FALSE, 51, 1056),
('Hanyang University', 'South Korea', NULL, FALSE, 51, 1057),
('Harbin Institute of Technology', 'China', NULL, FALSE, 51, 1058),
('Istanbul Technical University', 'Turkey', NULL, FALSE, 51, 1059),
('Karlsruhe Institute of Technology', 'Germany', NULL, FALSE, 51, 1060),
('Kyoto University', 'Japan', NULL, FALSE, 51, 1061),
('Loughborough University', 'United Kingdom', NULL, FALSE, 51, 1062),
('McGill University', 'Canada', NULL, FALSE, 51, 1063),
('Middle East Technical University', 'Turkey', NULL, FALSE, 51, 1064),
('Monash University', 'Australia', NULL, FALSE, 51, 1065),
('Nanjing University', 'China', NULL, FALSE, 51, 1066),
('Newcastle University', 'United Kingdom', NULL, FALSE, 51, 1067),
('Oxford Brookes University', 'United Kingdom', NULL, FALSE, 51, 1068),
('Peking University', 'China', NULL, FALSE, 51, 1069),
('Pennsylvania State University', 'United States', NULL, FALSE, 51, 1070),
('Purdue University West Lafayette', 'United States', NULL, FALSE, 51, 1071),
('Queensland University of Technology', 'Australia', NULL, FALSE, 51, 1072),
('RWTH Aachen University', 'Germany', NULL, FALSE, 51, 1073),
('Sapienza University of Rome', 'Italy', NULL, FALSE, 51, 1074),
('Shanghai Jiao Tong University', 'China', NULL, FALSE, 51, 1075),
('Technische Universität Darmstadt', 'Germany', NULL, FALSE, 51, 1076),
('Tecnológico de Monterrey', 'Mexico', NULL, FALSE, 51, 1077),
('Texas A&M University', 'United States', NULL, FALSE, 51, 1078),
('Chinese University of Hong Kong', 'Hong Kong', NULL, FALSE, 51, 1079),
('Hong Kong University of Science and Technology', 'Hong Kong', NULL, FALSE, 51, 1080),
('London School of Economics and Political Science', 'United Kingdom', NULL, FALSE, 51, 1081),
('University of Edinburgh', 'United Kingdom', NULL, FALSE, 51, 1082),
('The University of Queensland', 'Australia', NULL, FALSE, 51, 1083),
('Universidad Nacional Autónoma de México', 'Mexico', NULL, FALSE, 51, 1084),
('Universidad Nacional de Colombia', 'Colombia', NULL, FALSE, 51, 1085),
('Universidad de Buenos Aires', 'Argentina', NULL, FALSE, 51, 1086),
('University of Chile', 'Chile', NULL, FALSE, 51, 1087),
('Universidad de Los Andes', 'Colombia', NULL, FALSE, 51, 1088),
('Universitat Politècnica de València', 'Spain', NULL, FALSE, 51, 1089),
('University of Bath', 'United Kingdom', NULL, FALSE, 51, 1090),
('University of British Columbia', 'Canada', NULL, FALSE, 51, 1091),
('University of Illinois Urbana-Champaign', 'United States', NULL, FALSE, 51, 1092),
('University of Nottingham', 'United Kingdom', NULL, FALSE, 51, 1093),
('Universidade do Porto', 'Portugal', NULL, FALSE, 51, 1094),
('University of Southern California', 'United States', NULL, FALSE, 51, 1095),
('University of Technology Sydney', 'Australia', NULL, FALSE, 51, 1096),
('The University of Texas at Austin', 'United States', NULL, FALSE, 51, 1097),
('University of Waterloo', 'Canada', NULL, FALSE, 51, 1098),
('University of Stuttgart', 'Germany', NULL, FALSE, 51, 1099),
('Yonsei University', 'South Korea', NULL, FALSE, 51, 1100);


-- ===== from V14__bid_image_archiving.sql: bid image blob retention =====

-- Storage retention for bid images: the blob is deleted from object storage once a
-- bid is long dead, but the row is kept so bid history stays auditable.
ALTER TABLE rmtr_bid_image ADD COLUMN archived_at TIMESTAMP NULL;

CREATE INDEX idx_bid_image_archived_at ON rmtr_bid_image(archived_at);


-- ===== from V15__status_ledger.sql: append-only status ledger =====

-- Append-only status ledger for the money-touching streams.
--
-- The log is the source of truth for history; each entity's `status` column remains a
-- projection of it, written in the same transaction. Authorization reads the projection
-- (lockable, constrainable); audit reads the log.
--
-- NOTE: any future migration that alters these tables must drop and recreate the
-- append-only triggers below, which reject UPDATE, DELETE and TRUNCATE.

CREATE OR REPLACE FUNCTION rmtr_reject_mutation() RETURNS trigger AS $$
BEGIN
  RAISE EXCEPTION 'append-only table %: % is not permitted', TG_TABLE_NAME, TG_OP;
END;
$$ LANGUAGE plpgsql;


CREATE TABLE rmtr_project_status_log (
    id          BIGSERIAL PRIMARY KEY,
    project_id  BIGINT NOT NULL REFERENCES rmtr_project(id),
    actor_id    BIGINT REFERENCES rmtr_user(id),
    actor_type  VARCHAR(50) NOT NULL,
    action      VARCHAR(100) NOT NULL,
    from_status VARCHAR(50),
    to_status   VARCHAR(50) NOT NULL,
    metadata    JSONB,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_project_status_log_to CHECK (to_status IN (
        'DRAFT','PENDING_APPROVAL','REJECTED','OPEN','BIDDING_CLOSED','NEGOTIATION',
        'NEGOTIATION_EXPIRED','IN_PROGRESS','COMPLETED','CANCELLED','DELETED'))
);
CREATE INDEX idx_project_status_log_project_id ON rmtr_project_status_log(project_id);

CREATE TABLE rmtr_phase_payment_status_log (
    id               BIGSERIAL PRIMARY KEY,
    phase_payment_id BIGINT NOT NULL REFERENCES rmtr_phase_payment(id),
    actor_id         BIGINT REFERENCES rmtr_user(id),
    actor_type       VARCHAR(50) NOT NULL,
    action           VARCHAR(100) NOT NULL,
    from_status      VARCHAR(50),
    to_status        VARCHAR(50) NOT NULL,
    metadata         JSONB,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_phase_payment_status_log_to CHECK (to_status IN (
        'PENDING','COMPLETED','FAILED','EXPIRED'))
);
CREATE INDEX idx_phase_payment_status_log_payment_id
    ON rmtr_phase_payment_status_log(phase_payment_id);

CREATE TABLE rmtr_phase_disbursement_status_log (
    id              BIGSERIAL PRIMARY KEY,
    disbursement_id BIGINT NOT NULL REFERENCES rmtr_project_phase_disbursement(id),
    actor_id        BIGINT REFERENCES rmtr_user(id),
    actor_type      VARCHAR(50) NOT NULL,
    action          VARCHAR(100) NOT NULL,
    from_status     VARCHAR(50),
    to_status       VARCHAR(50) NOT NULL,
    metadata        JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_phase_disbursement_status_log_to CHECK (to_status IN (
        'PENDING','ACCEPTED','SUCCEEDED','FAILED','REVERSED'))
);
CREATE INDEX idx_phase_disbursement_status_log_disbursement_id
    ON rmtr_phase_disbursement_status_log(disbursement_id);

CREATE TABLE rmtr_token_purchase_status_log (
    id                BIGSERIAL PRIMARY KEY,
    token_purchase_id BIGINT NOT NULL REFERENCES rmtr_token_purchase(id),
    actor_id          BIGINT REFERENCES rmtr_user(id),
    actor_type        VARCHAR(50) NOT NULL,
    action            VARCHAR(100) NOT NULL,
    from_status       VARCHAR(50),
    to_status         VARCHAR(50) NOT NULL,
    metadata          JSONB,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_token_purchase_status_log_to CHECK (to_status IN (
        'PENDING','COMPLETED','FAILED','EXPIRED','CANCELLED'))
);
CREATE INDEX idx_token_purchase_status_log_purchase_id
    ON rmtr_token_purchase_status_log(token_purchase_id);

CREATE TABLE rmtr_subscription_status_log (
    id              BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL REFERENCES rmtr_subscription(id),
    actor_id        BIGINT REFERENCES rmtr_user(id),
    actor_type      VARCHAR(50) NOT NULL,
    action          VARCHAR(100) NOT NULL,
    from_status     VARCHAR(50),
    to_status       VARCHAR(50) NOT NULL,
    metadata        JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_subscription_status_log_to CHECK (to_status IN (
        'ACTIVE','PENDING','EXPIRED','CANCELLED'))
);
CREATE INDEX idx_subscription_status_log_subscription_id
    ON rmtr_subscription_status_log(subscription_id);


-- Seed a starting point per live entity. History before this migration does not exist and
-- is deliberately not fabricated.
INSERT INTO rmtr_project_status_log (project_id, actor_type, action, to_status)
    SELECT id, 'SYSTEM', 'LEDGER_INITIALIZED', status FROM rmtr_project;
INSERT INTO rmtr_phase_payment_status_log (phase_payment_id, actor_type, action, to_status)
    SELECT id, 'SYSTEM', 'LEDGER_INITIALIZED', status FROM rmtr_phase_payment;
INSERT INTO rmtr_phase_disbursement_status_log (disbursement_id, actor_type, action, to_status)
    SELECT id, 'SYSTEM', 'LEDGER_INITIALIZED', status FROM rmtr_project_phase_disbursement;
INSERT INTO rmtr_token_purchase_status_log (token_purchase_id, actor_type, action, to_status)
    SELECT id, 'SYSTEM', 'LEDGER_INITIALIZED', status FROM rmtr_token_purchase;
INSERT INTO rmtr_subscription_status_log (subscription_id, actor_type, action, to_status)
    SELECT id, 'SYSTEM', 'LEDGER_INITIALIZED', COALESCE(status, 'ACTIVE') FROM rmtr_subscription;


-- Append-only enforcement. Row-level triggers do not fire on TRUNCATE, so each table needs
-- a statement-level TRUNCATE trigger as well.
DO $$
DECLARE
  t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY[
    'rmtr_project_status_log',
    'rmtr_phase_payment_status_log',
    'rmtr_phase_disbursement_status_log',
    'rmtr_token_purchase_status_log',
    'rmtr_subscription_status_log',
    'rmtr_project_phase_log'
  ] LOOP
    EXECUTE format(
      'CREATE TRIGGER %I_append_only BEFORE UPDATE OR DELETE ON %I
         FOR EACH ROW EXECUTE FUNCTION rmtr_reject_mutation()', t, t);
    EXECUTE format(
      'CREATE TRIGGER %I_no_truncate BEFORE TRUNCATE ON %I
         FOR EACH STATEMENT EXECUTE FUNCTION rmtr_reject_mutation()', t, t);
  END LOOP;
END $$;


-- Database-level backstop against a second live payout for one phase. FAILED/REVERSED are
-- excluded so the existing retry flow keeps working.
CREATE UNIQUE INDEX idx_phase_disbursement_active
    ON rmtr_project_phase_disbursement(phase_id)
    WHERE status NOT IN ('FAILED', 'REVERSED');


-- ===== from V16__phase_deliverable_tagging.sql: tag deliverable files to bid line items =====

-- Tags each uploaded file to one of the deliverables named in the accepted bid, and records
-- which of those deliverables the client has approved.
--
-- The deliverable list itself is NOT copied here -- it stays owned by
-- rmtr_bid_payment_phase.deliverables. Files reference a position in that array rather than a
-- name, because names are not stable: a rename or a duplicate would silently regroup files.

ALTER TABLE rmtr_project_phase_deliverable
    ADD COLUMN deliverable_index INT;

CREATE INDEX idx_phase_deliverable_index
    ON rmtr_project_phase_deliverable(phase_id, deliverable_index);

COMMENT ON COLUMN rmtr_project_phase_deliverable.deliverable_index IS
    'Position in the accepted bid phase''s deliverables array. NULL for files uploaded before '
    'tagging existed; those render under "Other files".';


-- Current-state projection of the DELIVERABLE_APPROVED events in rmtr_project_phase_log.
-- The log is the durable record and is append-only; rows here are cleared when a revision is
-- requested, which erases no history.
--
-- The unique constraint is what makes double-approval impossible, so the approve path needs no
-- lock -- this is the path that flips a phase to APPROVED and unlocks the architect's payout.
CREATE TABLE rmtr_project_phase_deliverable_approval (
    id                BIGSERIAL PRIMARY KEY,
    phase_id          BIGINT NOT NULL REFERENCES rmtr_project_phase(id),
    deliverable_index INT NOT NULL,
    approved_by       BIGINT REFERENCES rmtr_user(id),
    approved_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_phase_deliverable_approval UNIQUE (phase_id, deliverable_index)
);

CREATE INDEX idx_phase_deliverable_approval_phase_id
    ON rmtr_project_phase_deliverable_approval(phase_id);


-- ===== from V17__deliverable_revision_requests.sql: per-deliverable revision requests =====

-- Records what the client asked to be redone, per deliverable.
--
-- The revision *count* stays on rmtr_project_phase (revisions_used / max_revisions): one request
-- covers however many deliverables the client selected and costs exactly one round. revision_round
-- is what pools them -- every row written by a single request shares it.
--
-- Rows are never deleted. Unlike the approval table, this is not a projection of anything: it is
-- the durable record of the instructions the architect has to work from, and previous rounds stay
-- readable as history. The phase log still carries the single REVISION_REQUESTED transition.

CREATE TABLE rmtr_project_phase_deliverable_revision (
    id                BIGSERIAL PRIMARY KEY,
    phase_id          BIGINT NOT NULL REFERENCES rmtr_project_phase(id),
    deliverable_index INT NOT NULL,
    revision_round    INT NOT NULL,
    notes             TEXT NOT NULL,
    requested_by      BIGINT REFERENCES rmtr_user(id),
    requested_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_phase_deliverable_revision UNIQUE (phase_id, deliverable_index, revision_round)
);

CREATE INDEX idx_phase_deliverable_revision_phase_id
    ON rmtr_project_phase_deliverable_revision(phase_id);

COMMENT ON COLUMN rmtr_project_phase_deliverable_revision.deliverable_index IS
    'Position in the accepted bid phase''s deliverables array, same convention as '
    'rmtr_project_phase_deliverable.deliverable_index.';

COMMENT ON COLUMN rmtr_project_phase_deliverable_revision.revision_round IS
    'phase.revisions_used after the request incremented it. Rows sharing a round were requested '
    'together and cost one round between them.';


-- ===== from V18__project_contract.sql: the signed contract and its two signatures =====

-- The signed agreement behind a project, and the two signatures on it.
--
-- Until now the workspace's Contract & Payment tab was a pure read model, recomputed on every
-- request from the accepted bid. Nothing recorded what the two parties actually agreed to, so
-- there was no artifact to point at when a phase went to dispute.
--
-- rmtr_project_contract is a snapshot, taken once per project the first time either party opens
-- the contract, of the accepted bid's terms: total fee, timeline, and every phase with its
-- amount, deliverables and revision rounds. terms_snapshot is the structured form; body_en and
-- body_id are the two renderings of it. Both bodies come from the same snapshot, so content_hash
-- is computed over the snapshot rather than over either body -- the two parties provably sign the
-- same terms whichever language they read.
--
-- Both tables are append-only. A contract that could be edited after signing would be worth
-- nothing as evidence, so UPDATE, DELETE and TRUNCATE are rejected by trigger. Nothing in the
-- application may call a setter on a loaded ProjectContract inside a transaction: Hibernate
-- dirty-checking would flush an UPDATE and hit the trigger.
--
-- Any future migration that alters these tables must drop and recreate both triggers.

CREATE TABLE rmtr_project_contract (
    id               BIGSERIAL PRIMARY KEY,
    project_id       BIGINT NOT NULL REFERENCES rmtr_project(id),
    bid_id           BIGINT NOT NULL REFERENCES rmtr_bid(id),
    template_version VARCHAR(16) NOT NULL,
    terms_snapshot   JSONB NOT NULL,
    body_en          TEXT NOT NULL,
    body_id          TEXT NOT NULL,
    content_hash     VARCHAR(64) NOT NULL,
    generated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_project_contract_project UNIQUE (project_id)
);

CREATE TABLE rmtr_project_contract_acceptance (
    id             BIGSERIAL PRIMARY KEY,
    contract_id    BIGINT NOT NULL REFERENCES rmtr_project_contract(id),
    project_id     BIGINT NOT NULL REFERENCES rmtr_project(id),
    user_id        BIGINT NOT NULL REFERENCES rmtr_user(id),
    party          VARCHAR(16) NOT NULL,
    signature_name VARCHAR(255) NOT NULL,
    content_hash   VARCHAR(64) NOT NULL,
    lang           VARCHAR(8) NOT NULL,
    accepted_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address     VARCHAR(64),
    user_agent     TEXT,
    CONSTRAINT chk_contract_acceptance_party CHECK (party IN ('CLIENT', 'ARCHITECT')),
    CONSTRAINT uq_contract_acceptance_party UNIQUE (contract_id, party)
);

CREATE INDEX idx_project_contract_acceptance_project
    ON rmtr_project_contract_acceptance(project_id);

COMMENT ON COLUMN rmtr_project_contract.terms_snapshot IS
    'Structured copy of the accepted bid''s terms at snapshot time. The bid can change afterwards; '
    'this cannot, and it is what was signed.';

COMMENT ON COLUMN rmtr_project_contract.content_hash IS
    'SHA-256 over the canonical terms_snapshot JSON prefixed by template_version. Language '
    'independent by design, so the en and id renderings share one hash.';

COMMENT ON COLUMN rmtr_project_contract_acceptance.project_id IS
    'Denormalised from the contract so signatures can be looked up by project without a join; '
    'the unique constraint on rmtr_project_contract.project_id keeps the two in step.';

COMMENT ON COLUMN rmtr_project_contract_acceptance.signature_name IS
    'Stored exactly as the signer typed it. Matching against the account name is done on a '
    'normalised copy, never by rewriting what they entered.';

COMMENT ON COLUMN rmtr_project_contract_acceptance.lang IS
    'Which rendering the signer was shown. Evidence of what they read, not what they agreed to.';

DO $$
DECLARE
  t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY[
    'rmtr_project_contract',
    'rmtr_project_contract_acceptance'
  ] LOOP
    EXECUTE format(
      'CREATE TRIGGER %I_append_only BEFORE UPDATE OR DELETE ON %I
         FOR EACH ROW EXECUTE FUNCTION rmtr_reject_mutation()', t, t);
    EXECUTE format(
      'CREATE TRIGGER %I_no_truncate BEFORE TRUNCATE ON %I
         FOR EACH STATEMENT EXECUTE FUNCTION rmtr_reject_mutation()', t, t);
  END LOOP;
END $$;


-- ===== from V19__iai_taxonomy.sql: move expertise and portfolio type onto the IAI classification =====

-- Move architect expertise and portfolio project type onto the IAI (Ikatan Arsitek Indonesia)
-- building classification. Data only -- both columns already hold free text wide enough for the
-- new codes (the longest is 27 characters).
--
-- The mapping is best-effort. Old values with no IAI counterpart (Sustainable Design, Urban
-- Planning, Interior Design, Landscape Architecture, Other, and the project-taxonomy codes
-- INTERIOR_ONLY / LANDSCAPE / INFRASTRUCTURE) are deliberately left untouched rather than
-- dropped: the frontend renders them as inert grey "legacy" chips, and the backend grandfathers
-- them so an affected profile can still be saved. Nothing is lost.

-- rmtr_porto.project_type holds a single value. It has been fed from two different lists: the old
-- PROJECT_TYPES picker, and -- when a finished project was archived into a portfolio -- raw
-- project-taxonomy category codes. Both are mapped here.
UPDATE rmtr_porto
SET project_type = CASE project_type
    WHEN 'Residential - Single Family' THEN 'K3_HUNIAN'
    WHEN 'RESIDENTIAL'                 THEN 'K3_HUNIAN'
    WHEN 'Residential - Multi Family'  THEN 'K2_HUNIAN'
    WHEN 'Commercial - Office'         THEN 'K2_KOMERSIAL'
    WHEN 'Commercial - Retail'         THEN 'K2_KOMERSIAL'
    WHEN 'COMMERCIAL'                  THEN 'K2_KOMERSIAL'
    WHEN 'MIXED_USE'                   THEN 'K2_KOMERSIAL'
    WHEN 'Hospitality'                 THEN 'K3_KOMERSIAL'
    WHEN 'Institutional'               THEN 'K2_KOMUNITAS'
    WHEN 'INSTITUTIONAL'               THEN 'K2_KOMUNITAS'
    WHEN 'Cultural'                    THEN 'K3_KOMUNITAS'
    WHEN 'Renovation'                  THEN 'K3_LAINNYA'
    WHEN 'INDUSTRIAL'                  THEN 'K2_INDUSTRI'
    ELSE project_type
  END
WHERE project_type IS NOT NULL;

-- rmtr_architect.expertise is a JSONB array, and one old tag can fan out to several IAI pairs
-- (an architect who said "Residential" works across Kategori 1, 2 and 3). Each array is rebuilt
-- from its elements; unmapped elements pass through unchanged, and DISTINCT collapses the
-- overlaps created by the fan-out.
WITH mapping(old_tag, new_codes) AS (
  VALUES
    ('Residential',          ARRAY['K1_HUNIAN', 'K2_HUNIAN', 'K3_HUNIAN']),
    ('Commercial',           ARRAY['K1_KOMERSIAL', 'K2_KOMERSIAL']),
    ('Renovation',           ARRAY['K3_LAINNYA']),
    ('Historic Preservation', ARRAY['K3_LAINNYA']),
    ('Institutional',        ARRAY['K2_KOMUNITAS', 'K2_PENDIDIKAN']),
    ('Industrial',           ARRAY['K1_INDUSTRI', 'K2_INDUSTRI']),
    ('Mixed-Use',            ARRAY['K2_KOMERSIAL']),
    ('Hospitality',          ARRAY['K3_KOMERSIAL'])
),
rebuilt AS (
  SELECT a.id,
         jsonb_agg(DISTINCT translated.code) AS expertise
  FROM rmtr_architect a
  CROSS JOIN LATERAL jsonb_array_elements_text(a.expertise) AS tag(value)
  CROSS JOIN LATERAL (
    SELECT unnest(m.new_codes) AS code FROM mapping m WHERE m.old_tag = tag.value
    UNION ALL
    SELECT tag.value WHERE NOT EXISTS (SELECT 1 FROM mapping m WHERE m.old_tag = tag.value)
  ) AS translated
  WHERE a.expertise IS NOT NULL
    AND jsonb_typeof(a.expertise) = 'array'
  GROUP BY a.id
)
UPDATE rmtr_architect a
SET expertise = rebuilt.expertise
FROM rebuilt
WHERE a.id = rebuilt.id
  AND a.expertise IS DISTINCT FROM rebuilt.expertise;
