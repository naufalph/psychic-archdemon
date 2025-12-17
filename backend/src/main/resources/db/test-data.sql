-- ============================================
-- Test Data for Rumantra Platform
-- ============================================
-- Password for all users: password123
-- Bcrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- ============================================

-- Clean up existing test data (optional - comment out if you want to keep existing data)
-- DELETE FROM rmtr_bid_usage_log WHERE architect_id IN (SELECT id FROM rmtr_architect WHERE user_id IN (SELECT id FROM rmtr_user WHERE email LIKE 'test%@rumantra.com'));
-- DELETE FROM rmtr_bid_quota WHERE architect_id IN (SELECT id FROM rmtr_architect WHERE user_id IN (SELECT id FROM rmtr_user WHERE email LIKE 'test%@rumantra.com'));
-- DELETE FROM rmtr_subscription WHERE architect_id IN (SELECT id FROM rmtr_architect WHERE user_id IN (SELECT id FROM rmtr_user WHERE email LIKE 'test%@rumantra.com'));
-- DELETE FROM rmtr_project WHERE client_id IN (SELECT id FROM rmtr_client WHERE user_id IN (SELECT id FROM rmtr_user WHERE email LIKE 'test%@rumantra.com'));
-- DELETE FROM rmtr_architect WHERE user_id IN (SELECT id FROM rmtr_user WHERE email LIKE 'test%@rumantra.com');
-- DELETE FROM rmtr_client WHERE user_id IN (SELECT id FROM rmtr_user WHERE email LIKE 'test%@rumantra.com');
-- DELETE FROM rmtr_user WHERE email LIKE 'test%@rumantra.com';

-- ============================================
-- 1. Create Test Users
-- ============================================

-- Architect User 1
INSERT INTO rmtr_user (password_hash, email, social_type, first_nm, last_nm, is_email_verified, is_active, is_superuser, created_at)
VALUES ('$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'test.architect1@rumantra.com', 'EMAIL', 'Budi', 'Prasetyo', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP);

-- Architect User 2
INSERT INTO rmtr_user (password_hash, email, social_type, first_nm, last_nm, is_email_verified, is_active, is_superuser, created_at)
VALUES ('$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'test.architect2@rumantra.com', 'EMAIL', 'Siti', 'Nurhaliza', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP);

-- Client User 1
INSERT INTO rmtr_user (password_hash, email, social_type, first_nm, last_nm, is_email_verified, is_active, is_superuser, created_at)
VALUES ('$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'test.client1@rumantra.com', 'EMAIL', 'Ahmad', 'Hidayat', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP);

-- Client User 2
INSERT INTO rmtr_user (password_hash, email, social_type, first_nm, last_nm, is_email_verified, is_active, is_superuser, created_at)
VALUES ('$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'test.client2@rumantra.com', 'EMAIL', 'Dewi', 'Lestari', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP);

-- Superuser (for validation)
INSERT INTO rmtr_user (password_hash, email, social_type, first_nm, last_nm, is_email_verified, is_active, is_superuser, created_at)
VALUES ('$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'test.admin@rumantra.com', 'EMAIL', 'Admin', 'Rumantra', TRUE, TRUE, TRUE, CURRENT_TIMESTAMP);

-- ============================================
-- 2. Create Architect Profiles
-- ============================================

INSERT INTO rmtr_architect (user_id, company_name, company_site, contact_name, category, phone_num, ktp_num, is_ktp_verified, npwp, is_npwp_verified, success_match, success_project)
VALUES
    ((SELECT id FROM rmtr_user WHERE email = 'test.architect1@rumantra.com'),
     'Prasetyo Architects',
     'https://praseyoarchitects.com',
     'Budi Prasetyo',
     'SENIOR',
     '+628123456789',
     '3201010101010001',
     TRUE,
     '123456789012345',
     TRUE,
     15,
     12);

INSERT INTO rmtr_architect (user_id, company_name, company_site, contact_name, category, phone_num, ktp_num, is_ktp_verified, npwp, is_npwp_verified, success_match, success_project)
VALUES
    ((SELECT id FROM rmtr_user WHERE email = 'test.architect2@rumantra.com'),
     'Nurhaliza Design Studio',
     'https://nurhaliza.design',
     'Siti Nurhaliza',
     'JUNIOR',
     '+628987654321',
     '3201020202020002',
     TRUE,
     '987654321098765',
     TRUE,
     8,
     6);

-- ============================================
-- 3. Create Client Profiles
-- ============================================

INSERT INTO rmtr_client (user_id, phone_num, is_phonenum_verified, ktp_num, is_ktp_verified, project_match, project_finished)
VALUES
    ((SELECT id FROM rmtr_user WHERE email = 'test.client1@rumantra.com'),
     '+628111222333',
     TRUE,
     '3201030303030003',
     TRUE,
     3,
     2);

INSERT INTO rmtr_client (user_id, phone_num, is_phonenum_verified, ktp_num, is_ktp_verified, project_match, project_finished)
VALUES
    ((SELECT id FROM rmtr_user WHERE email = 'test.client2@rumantra.com'),
     '+628444555666',
     TRUE,
     '3201040404040004',
     TRUE,
     5,
     4);

-- ============================================
-- 4. Create Bid Quotas for Architects (FREE tier)
-- ============================================

INSERT INTO rmtr_bid_quota (architect_id, tier, tokens_allocated, tokens_remaining, created_at)
VALUES
    ((SELECT id FROM rmtr_architect WHERE user_id = (SELECT id FROM rmtr_user WHERE email = 'test.architect1@rumantra.com')),
     'FREE',
     1,
     1,
     CURRENT_TIMESTAMP);

INSERT INTO rmtr_bid_quota (architect_id, tier, tokens_allocated, tokens_remaining, created_at)
VALUES
    ((SELECT id FROM rmtr_architect WHERE user_id = (SELECT id FROM rmtr_user WHERE email = 'test.architect2@rumantra.com')),
     'FREE',
     1,
     0,  -- Already used their token
     CURRENT_TIMESTAMP);

-- ============================================
-- 5. Create Test Projects (OPEN status)
-- ============================================

-- Project 1: Modern Minimalist House
INSERT INTO rmtr_project (
    client_id,
    budget_min,
    budget_max,
    project_category,
    building_function,
    estimated_build_area,
    number_of_floors,
    owns_land,
    has_legal_documents,
    scope_of_work,
    deliverables,
    design_preferences,
    contact_person,
    expected_start_date,
    status,
    created_at
)
VALUES (
    (SELECT id FROM rmtr_client WHERE user_id = (SELECT id FROM rmtr_user WHERE email = 'test.client1@rumantra.com')),
    200000000,  -- Rp 2,000,000 in cents
    250000000,  -- Rp 2,500,000 in cents
    'Residential',
    'Modern Minimalist House',
    150,
    2,
    TRUE,
    TRUE,
    'Design modern minimalist house with open space concept and optimal natural lighting. Include site plan, floor plans, elevations, 3D renderings, and construction documents.',
    '["Site Plan", "Floor Plans", "Elevations", "3D Renderings", "Construction Documents"]',
    'Modern minimalist style with clean lines, large windows, neutral color palette (white, grey, black), natural materials like wood and stone, and indoor-outdoor flow.',
    'Ahmad Hidayat',
    CURRENT_DATE + INTERVAL '1 month',
    'OPEN',
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
);

-- Project 2: Villa Exterior & Landscape
INSERT INTO rmtr_project (
    client_id,
    budget_min,
    budget_max,
    project_category,
    building_function,
    estimated_build_area,
    number_of_floors,
    owns_land,
    has_legal_documents,
    scope_of_work,
    deliverables,
    design_preferences,
    contact_person,
    expected_start_date,
    status,
    created_at
)
VALUES (
    (SELECT id FROM rmtr_client WHERE user_id = (SELECT id FROM rmtr_user WHERE email = 'test.client1@rumantra.com')),
    300000000,  -- Rp 3,000,000 in cents
    350000000,  -- Rp 3,500,000 in cents
    'Residential',
    'Villa Exterior & Landscape',
    300,
    2,
    TRUE,
    TRUE,
    'Develop the complete exterior design of the villa in coordination with the approved architectural layout. Ensure that the external design aligns precisely with the functional distribution and internal layout of the spaces, especially living areas and their visual connection with outdoor spaces. Use only one color natural stone for the façade cladding to achieve a cohesive and elegant appearance.',
    '["Exterior Design", "Landscape Plan", "Material Specifications", "3D Renderings", "Lighting Design"]',
    'Modern or new classic style villa with natural stone façade, integration of indoor-outdoor spaces, tropical landscaping, and sophisticated lighting design.',
    'Ahmad Hidayat',
    CURRENT_DATE + INTERVAL '2 months',
    'OPEN',
    CURRENT_TIMESTAMP - INTERVAL '3 hours'
);

-- Project 3: Office Building Renovation
INSERT INTO rmtr_project (
    client_id,
    budget_min,
    budget_max,
    project_category,
    building_function,
    estimated_build_area,
    number_of_floors,
    owns_land,
    has_legal_documents,
    scope_of_work,
    deliverables,
    design_preferences,
    contact_person,
    expected_start_date,
    status,
    created_at
)
VALUES (
    (SELECT id FROM rmtr_client WHERE user_id = (SELECT id FROM rmtr_user WHERE email = 'test.client2@rumantra.com')),
    150000000,  -- Rp 1,500,000 in cents
    200000000,  -- Rp 2,000,000 in cents
    'Commercial',
    'Office Building Renovation',
    500,
    3,
    FALSE,
    TRUE,
    'Renovation of existing office building to create modern, collaborative workspace. Include new layout design, facade update, interior design for common areas, and energy-efficient solutions.',
    '["Renovation Plans", "Interior Design", "Facade Design", "3D Renderings", "Material Specifications"]',
    'Contemporary professional style with collaborative spaces, natural lighting, sustainable materials, and flexible workspaces.',
    'Dewi Lestari',
    CURRENT_DATE + INTERVAL '3 months',
    'OPEN',
    CURRENT_TIMESTAMP - INTERVAL '5 hours'
);

-- Project 4: Cafe Design
INSERT INTO rmtr_project (
    client_id,
    budget_min,
    budget_max,
    project_category,
    building_function,
    estimated_build_area,
    number_of_floors,
    owns_land,
    has_legal_documents,
    scope_of_work,
    deliverables,
    design_preferences,
    contact_person,
    expected_start_date,
    status,
    created_at
)
VALUES (
    (SELECT id FROM rmtr_client WHERE user_id = (SELECT id FROM rmtr_user WHERE email = 'test.client2@rumantra.com')),
    100000000,  -- Rp 1,000,000 in cents
    150000000,  -- Rp 1,500,000 in cents
    'Commercial',
    'Cozy Cafe Design',
    120,
    1,
    FALSE,
    TRUE,
    'Design a cozy and Instagram-worthy cafe with indoor and outdoor seating. Focus on creating a welcoming atmosphere with good lighting, comfortable seating, and aesthetic interior design.',
    '["Floor Plan", "Interior Design", "Furniture Layout", "Lighting Design", "3D Renderings"]',
    'Industrial-modern fusion with exposed brick, warm wood tones, plants, and comfortable seating. Instagram-worthy photo spots.',
    'Dewi Lestari',
    CURRENT_DATE + INTERVAL '6 weeks',
    'OPEN',
    CURRENT_TIMESTAMP - INTERVAL '8 hours'
);

-- Project 5: Townhouse Complex
INSERT INTO rmtr_project (
    client_id,
    budget_min,
    budget_max,
    project_category,
    building_function,
    estimated_build_area,
    number_of_floors,
    owns_land,
    has_legal_documents,
    scope_of_work,
    deliverables,
    design_preferences,
    contact_person,
    expected_start_date,
    status,
    created_at
)
VALUES (
    (SELECT id FROM rmtr_client WHERE user_id = (SELECT id FROM rmtr_user WHERE email = 'test.client1@rumantra.com')),
    500000000,  -- Rp 5,000,000 in cents
    600000000,  -- Rp 6,000,000 in cents
    'Residential',
    'Townhouse Complex (8 Units)',
    1200,
    3,
    TRUE,
    TRUE,
    'Design a townhouse complex with 8 units. Each unit should have 3 floors, private parking, and small backyard. Include site plan, typical unit floor plans, elevations, and landscape design for common areas.',
    '["Site Master Plan", "Unit Floor Plans", "Elevations", "Landscape Design", "3D Renderings", "Construction Documents"]',
    'Contemporary tropical design with efficient space planning, natural ventilation, and community-oriented layout.',
    'Ahmad Hidayat',
    CURRENT_DATE + INTERVAL '4 months',
    'OPEN',
    CURRENT_TIMESTAMP - INTERVAL '12 hours'
);

-- ============================================
-- End of Test Data
-- ============================================

-- Verify the data was inserted correctly
SELECT
    u.email,
    u.first_nm || ' ' || u.last_nm as full_name,
    CASE
        WHEN a.id IS NOT NULL THEN 'ARCHITECT'
        WHEN c.id IS NOT NULL THEN 'CLIENT'
        WHEN u.is_superuser THEN 'SUPERUSER'
        ELSE 'USER'
    END as role,
    u.is_email_verified,
    u.is_active
FROM rmtr_user u
LEFT JOIN rmtr_architect a ON a.user_id = u.id
LEFT JOIN rmtr_client c ON c.user_id = u.id
WHERE u.email LIKE 'test%@rumantra.com'
ORDER BY u.email;

-- Check projects
SELECT
    p.id,
    p.building_function,
    p.budget_min / 100 as budget_min_rupiah,
    p.budget_max / 100 as budget_max_rupiah,
    p.status,
    u.email as client_email,
    p.created_at
FROM rmtr_project p
JOIN rmtr_client c ON c.id = p.client_id
JOIN rmtr_user u ON u.id = c.user_id
WHERE p.status = 'OPEN'
ORDER BY p.created_at DESC;
