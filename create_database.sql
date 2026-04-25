-- Barangay Management System Database Creation Script
-- This script creates all necessary tables for the enhanced barangay system

-- =====================================================
-- 1. USERS TABLE
-- =====================================================
CREATE TABLE users (
    id AUTOINCREMENT PRIMARY KEY,
    email TEXT(255) UNIQUE,
    password TEXT(255),
    role TEXT(50),
    first_name TEXT(100),
    last_name TEXT(100),
    contact_number TEXT(20),
    address MEMO,
    date_of_birth TEXT(50),
    gender TEXT(20),
    civil_status TEXT(50),
    occupation TEXT(100),
    profile_picture TEXT(500),
    is_active YESNO,
    created_at TEXT(50),
    updated_at TEXT(50)
);

-- =====================================================
-- 2. DOCUMENT TYPES TABLE
-- =====================================================
CREATE TABLE document_types (
    id AUTOINCREMENT PRIMARY KEY,
    type_name TEXT(255),
    description MEMO,
    base_fee CURRENCY,
    processing_days INTEGER,
    requirements MEMO,
    is_active YESNO,
    created_at TEXT(50)
);

-- =====================================================
-- 3. DOCUMENT REQUESTS TABLE
-- =====================================================
CREATE TABLE document_requests (
    id AUTOINCREMENT PRIMARY KEY,
    request_id TEXT(255) UNIQUE,
    user_email TEXT(255),
    document_type TEXT(255),
    full_name TEXT(255),
    age INTEGER,
    gender TEXT(50),
    birth_place TEXT(255),
    birth_date TEXT(50),
    civil_status TEXT(50),
    occupation TEXT(255),
    address MEMO,
    years_residency INTEGER,
    head_of_family TEXT(255),
    family_members INTEGER,
    monthly_income CURRENCY,
    income_source TEXT(255),
    contact_number TEXT(50),
    email_address TEXT(255),
    purpose TEXT(255),
    business_name TEXT(255),
    business_type TEXT(255),
    business_address MEMO,
    proof_of_residency TEXT(500),
    proof_of_income TEXT(500),
    business_permit_copy TEXT(500),
    other_documents TEXT(500),
    notes MEMO,
    admin_notes MEMO,
    status TEXT(50),
    priority TEXT(20),
    assigned_to TEXT(255),
    date_requested TEXT(50),
    date_approved TEXT(50),
    date_completed TEXT(50),
    date_released TEXT(50),
    processing_fee CURRENCY,
    is_paid YESNO,
    payment_reference TEXT(255),
    document_path TEXT(500),
    created_at TEXT(50),
    updated_at TEXT(50)
);

-- =====================================================
-- 4. PAYMENTS TABLE
-- =====================================================
CREATE TABLE payments (
    id AUTOINCREMENT PRIMARY KEY,
    payment_id TEXT(255) UNIQUE,
    user_email TEXT(255),
    reference_number TEXT(255),
    document_request_id TEXT(255),
    payment_type TEXT(100),
    description TEXT(255),
    amount CURRENCY,
    payment_method TEXT(50),
    transaction_id TEXT(255),
    paymongo_link_id TEXT(255),
    status TEXT(50),
    payment_date TEXT(50),
    due_date TEXT(50),
    receipt_number TEXT(255),
    receipt_path TEXT(500),
    notes MEMO,
    processed_by TEXT(255),
    archived YESNO,
    created_at TEXT(50),
    updated_at TEXT(50)
);

-- =====================================================
-- 5. COMPLAINTS TABLE
-- =====================================================
CREATE TABLE complaints (
    id AUTOINCREMENT PRIMARY KEY,
    complaint_id TEXT(255) UNIQUE,
    user_email TEXT(255),
    complainant_name TEXT(255),
    complainant_contact TEXT(50),
    complainant_address MEMO,
    incident_type TEXT(255),
    incident_category TEXT(100),
    location TEXT(255),
    incident_date TEXT(50),
    date_filed TEXT(50),
    status TEXT(50),
    priority TEXT(20),
    incident_details MEMO,
    evidence_photos TEXT(1000),
    witness_info MEMO,
    admin_response MEMO,
    resolution MEMO,
    assigned_to TEXT(255),
    resolved_date TEXT(50),
    is_anonymous YESNO,
    is_read YESNO,
    created_at TEXT(50),
    updated_at TEXT(50)
);

-- =====================================================
-- 6. ANNOUNCEMENTS TABLE
-- =====================================================
CREATE TABLE announcements (
    id AUTOINCREMENT PRIMARY KEY,
    announcement_id TEXT(255) UNIQUE,
    title TEXT(255),
    content MEMO,
    summary TEXT(500),
    priority TEXT(50),
    category TEXT(100),
    target_audience TEXT(100),
    posted_by TEXT(255),
    date_posted TEXT(50),
    expiry_date TEXT(50),
    is_active YESNO,
    is_featured YESNO,
    image_path TEXT(500),
    attachment_path TEXT(500),
    view_count INTEGER,
    created_at TEXT(50),
    updated_at TEXT(50)
);

-- =====================================================
-- 7. NOTIFICATIONS TABLE
-- =====================================================
CREATE TABLE notifications (
    notif_id AUTOINCREMENT PRIMARY KEY,
    user_email TEXT(255),
    type TEXT(50),
    title TEXT(255),
    message MEMO,
    reference_id TEXT(255),
    reference_type TEXT(50),
    is_read TEXT(10),
    priority TEXT(20),
    action_url TEXT(500),
    created_at TEXT(50),
    read_at TEXT(50)
);

-- =====================================================
-- 8. AUDIT LOGS TABLE
-- =====================================================
CREATE TABLE audit_logs (
    id AUTOINCREMENT PRIMARY KEY,
    user_email TEXT(255),
    action TEXT(255),
    table_name TEXT(100),
    record_id TEXT(255),
    old_values MEMO,
    new_values MEMO,
    ip_address TEXT(50),
    user_agent TEXT(500),
    created_at TEXT(50)
);

-- =====================================================
-- 9. SYSTEM SETTINGS TABLE
-- =====================================================
CREATE TABLE system_settings (
    id AUTOINCREMENT PRIMARY KEY,
    setting_key TEXT(255) UNIQUE,
    setting_value MEMO,
    setting_type TEXT(50),
    description TEXT(500),
    is_public YESNO,
    updated_by TEXT(255),
    updated_at TEXT(50)
);

-- =====================================================
-- 10. BARANGAY OFFICIALS TABLE
-- =====================================================
CREATE TABLE barangay_officials (
    id AUTOINCREMENT PRIMARY KEY,
    position TEXT(255),
    full_name TEXT(255),
    contact_number TEXT(50),
    email TEXT(255),
    address MEMO,
    term_start TEXT(50),
    term_end TEXT(50),
    is_active YESNO,
    photo_path TEXT(500),
    bio MEMO,
    created_at TEXT(50),
    updated_at TEXT(50)
);

-- =====================================================
-- SAMPLE DATA INSERTION
-- =====================================================

-- Insert sample users
INSERT INTO users (email, password, role, first_name, last_name, contact_number, address, date_of_birth, gender, civil_status, occupation, is_active, created_at, updated_at) VALUES
('admin@barangay.com', 'admin123', 'admin', 'Juan', 'Dela Cruz', '09123456789', 'Barangay San Isidro Hall', '1980-01-15', 'Male', 'Married', 'Barangay Captain', True, '2024-06-01 00:00:00', '2024-06-01 00:00:00'),
('resident@email.com', 'resident123', 'resident', 'Maria', 'Santos', '09987654321', '123 Main Street, San Isidro', '1990-05-20', 'Female', 'Single', 'Teacher', True, '2024-06-01 00:00:00', '2024-06-01 00:00:00'),
('john.doe@email.com', 'password123', 'resident', 'John', 'Doe', '09111222333', '456 Oak Avenue, San Isidro', '1985-12-10', 'Male', 'Married', 'Engineer', True, '2024-06-01 00:00:00', '2024-06-01 00:00:00');

-- Insert document types
INSERT INTO document_types (type_name, description, base_fee, processing_days, requirements, is_active, created_at) VALUES
('Barangay Clearance', 'Certificate of good moral character and residence', 100.00, 3, 'Valid ID, Proof of Residency, Cedula', True, '2024-06-01 00:00:00'),
('Certificate of Residency', 'Proof of residence in the barangay', 50.00, 2, 'Valid ID, Utility Bill, Barangay ID', True, '2024-06-01 00:00:00'),
('Certificate of Indigency', 'Certificate for low-income families', 30.00, 5, 'Valid ID, Proof of Income, Family Members List', True, '2024-06-01 00:00:00'),
('Business Permit', 'Permit to operate business within barangay', 500.00, 7, 'Business Registration, Tax Clearance, Location Clearance', True, '2024-06-01 00:00:00');

-- Insert sample document requests
INSERT INTO document_requests (request_id, user_email, document_type, full_name, age, gender, birth_date, civil_status, address, years_residency, contact_number, email_address, purpose, status, processing_fee, is_paid, date_requested, created_at, updated_at) VALUES
('REQ-2024-001', 'resident@email.com', 'Barangay Clearance', 'Maria Santos', 34, 'Female', '1990-05-20', 'Single', '123 Main Street, San Isidro', 5, '09987654321', 'resident@email.com', 'Employment', 'Ready', 100.00, True, '2024-06-10', '2024-06-10 00:00:00', '2024-06-10 00:00:00'),
('REQ-2024-002', 'john.doe@email.com', 'Certificate of Residency', 'John Doe', 39, 'Male', '1985-12-10', 'Married', '456 Oak Avenue, San Isidro', 10, '09111222333', 'john.doe@email.com', 'School Requirements', 'In Progress', 50.00, False, '2024-06-12', '2024-06-12 00:00:00', '2024-06-12 00:00:00'),
('REQ-2024-003', 'resident@email.com', 'Certificate of Indigency', 'Maria Santos', 34, 'Female', '1990-05-20', 'Single', '123 Main Street, San Isidro', 5, '09987654321', 'resident@email.com', 'Medical Assistance', 'Pending', 30.00, False, '2024-06-13', '2024-06-13 00:00:00', '2024-06-13 00:00:00');

-- Insert sample payments
INSERT INTO payments (payment_id, user_email, reference_number, document_request_id, payment_type, description, amount, payment_method, status, payment_date, receipt_number, archived, created_at, updated_at) VALUES
('PAY-2024-001', 'resident@email.com', 'REQ-2024-001', 'REQ-2024-001', 'Document Fee', 'Barangay Clearance Fee', 100.00, 'Cash', 'Paid', '2024-06-10', 'RCT-001', False, '2024-06-10 00:00:00', '2024-06-10 00:00:00'),
('PAY-2024-002', 'john.doe@email.com', 'REQ-2024-002', 'REQ-2024-002', 'Document Fee', 'Certificate of Residency Fee', 50.00, 'Online', 'Pending', NULL, NULL, False, '2024-06-12 00:00:00', '2024-06-12 00:00:00');

-- Insert sample announcements
INSERT INTO announcements (announcement_id, title, content, summary, priority, category, target_audience, posted_by, date_posted, is_active, is_featured, view_count, created_at, updated_at) VALUES
('ANN-2024-001', 'Community Health Program', 'Join our free health screening program every Saturday at the Barangay Hall. Services include blood pressure monitoring, diabetes screening, and health consultations.', 'Free health screening every Saturday', 'High', 'Health', 'All Residents', 'admin@barangay.com', '2024-06-01', True, True, 0, '2024-06-01 00:00:00', '2024-06-01 00:00:00'),
('ANN-2024-002', 'Barangay Assembly Meeting', 'Monthly barangay assembly meeting scheduled for June 25, 2024 at 7:00 PM. All residents are encouraged to attend.', 'Monthly assembly meeting on June 25', 'Medium', 'Government', 'All Residents', 'admin@barangay.com', '2024-06-15', True, False, 0, '2024-06-15 00:00:00', '2024-06-15 00:00:00');

-- Insert system settings
INSERT INTO system_settings (setting_key, setting_value, setting_type, description, is_public, updated_by, updated_at) VALUES
('barangay_name', 'Barangay San Isidro', 'text', 'Official name of the barangay', True, 'admin@barangay.com', '2024-06-01 00:00:00'),
('office_hours', 'Monday-Friday 8:00 AM - 5:00 PM', 'text', 'Official office hours', True, 'admin@barangay.com', '2024-06-01 00:00:00'),
('contact_number', '(02) 123-4567', 'text', 'Main contact number', True, 'admin@barangay.com', '2024-06-01 00:00:00'),
('email_address', 'info@barangaysanisidro.gov.ph', 'email', 'Official email address', True, 'admin@barangay.com', '2024-06-01 00:00:00'),
('address', 'Barangay San Isidro Hall, San Isidro, Metro Manila', 'text', 'Official address', True, 'admin@barangay.com', '2024-06-01 00:00:00'),
('document_processing_days', '3-7', 'text', 'Standard processing time for documents', True, 'admin@barangay.com', '2024-06-01 00:00:00'),
('payment_methods', 'Cash, Online Payment (PayMongo)', 'text', 'Available payment methods', True, 'admin@barangay.com', '2024-06-01 00:00:00');

-- Insert barangay officials
INSERT INTO barangay_officials (position, full_name, contact_number, email, address, term_start, term_end, is_active, created_at, updated_at) VALUES
('Barangay Captain', 'Juan Dela Cruz', '09123456789', 'captain@barangaysanisidro.gov.ph', 'San Isidro, Metro Manila', '2022-01-01', '2025-12-31', True, '2024-06-01 00:00:00', '2024-06-01 00:00:00'),
('Barangay Secretary', 'Maria Garcia', '09234567890', 'secretary@barangaysanisidro.gov.ph', 'San Isidro, Metro Manila', '2022-01-01', '2025-12-31', True, '2024-06-01 00:00:00', '2024-06-01 00:00:00'),
('Barangay Treasurer', 'Pedro Rodriguez', '09345678901', 'treasurer@barangaysanisidro.gov.ph', 'San Isidro, Metro Manila', '2022-01-01', '2025-12-31', True, '2024-06-01 00:00:00', '2024-06-01 00:00:00');

-- =====================================================
-- INDEXES FOR PERFORMANCE (Optional)
-- =====================================================

-- Create indexes for frequently queried fields
-- Note: Access databases automatically create indexes for primary keys and unique fields

-- Index on user email for faster lookups
-- CREATE INDEX idx_users_email ON users(email);

-- Index on document request status for filtering
-- CREATE INDEX idx_document_requests_status ON document_requests(status);

-- Index on payment status for filtering
-- CREATE INDEX idx_payments_status ON payments(status);

-- Index on notification read status
-- CREATE INDEX idx_notifications_read ON notifications(is_read);

-- =====================================================
-- END OF SCRIPT
-- =====================================================