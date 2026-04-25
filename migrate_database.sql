-- Database Migration Script for Barangay Management System
-- This script updates existing databases to the new enhanced structure
-- Run this script if you have an existing database that needs to be updated

-- =====================================================
-- BACKUP REMINDER
-- =====================================================
-- IMPORTANT: Always backup your existing database before running this migration!
-- Copy your barangay.accdb file to a safe location before proceeding.

-- =====================================================
-- 1. ADD NEW COLUMNS TO EXISTING TABLES
-- =====================================================

-- Update users table with new fields
ALTER TABLE users ADD COLUMN first_name TEXT(100);
ALTER TABLE users ADD COLUMN last_name TEXT(100);
ALTER TABLE users ADD COLUMN contact_number TEXT(20);
ALTER TABLE users ADD COLUMN address MEMO;
ALTER TABLE users ADD COLUMN date_of_birth TEXT(50);
ALTER TABLE users ADD COLUMN gender TEXT(20);
ALTER TABLE users ADD COLUMN civil_status TEXT(50);
ALTER TABLE users ADD COLUMN occupation TEXT(100);
ALTER TABLE users ADD COLUMN profile_picture TEXT(500);
ALTER TABLE users ADD COLUMN is_active YESNO;
ALTER TABLE users ADD COLUMN updated_at TEXT(50);

-- Update document_requests table with new fields
ALTER TABLE document_requests ADD COLUMN business_name TEXT(255);
ALTER TABLE document_requests ADD COLUMN business_type TEXT(255);
ALTER TABLE document_requests ADD COLUMN business_address MEMO;
ALTER TABLE document_requests ADD COLUMN business_permit_copy TEXT(500);
ALTER TABLE document_requests ADD COLUMN other_documents TEXT(500);
ALTER TABLE document_requests ADD COLUMN admin_notes MEMO;
ALTER TABLE document_requests ADD COLUMN priority TEXT(20);
ALTER TABLE document_requests ADD COLUMN assigned_to TEXT(255);
ALTER TABLE document_requests ADD COLUMN date_approved TEXT(50);
ALTER TABLE document_requests ADD COLUMN date_released TEXT(50);
ALTER TABLE document_requests ADD COLUMN processing_fee CURRENCY;
ALTER TABLE document_requests ADD COLUMN is_paid YESNO;
ALTER TABLE document_requests ADD COLUMN payment_reference TEXT(255);
ALTER TABLE document_requests ADD COLUMN document_path TEXT(500);
ALTER TABLE document_requests ADD COLUMN created_at TEXT(50);
ALTER TABLE document_requests ADD COLUMN updated_at TEXT(50);

-- Update payments table with new fields
ALTER TABLE payments ADD COLUMN user_email TEXT(255);
ALTER TABLE payments ADD COLUMN document_request_id TEXT(255);
ALTER TABLE payments ADD COLUMN description TEXT(255);
ALTER TABLE payments ADD COLUMN payment_method TEXT(50);
ALTER TABLE payments ADD COLUMN transaction_id TEXT(255);
ALTER TABLE payments ADD COLUMN paymongo_link_id TEXT(255);
ALTER TABLE payments ADD COLUMN payment_date TEXT(50);
ALTER TABLE payments ADD COLUMN due_date TEXT(50);
ALTER TABLE payments ADD COLUMN receipt_number TEXT(255);
ALTER TABLE payments ADD COLUMN receipt_path TEXT(500);
ALTER TABLE payments ADD COLUMN notes MEMO;
ALTER TABLE payments ADD COLUMN processed_by TEXT(255);
ALTER TABLE payments ADD COLUMN created_at TEXT(50);
ALTER TABLE payments ADD COLUMN updated_at TEXT(50);

-- Update complaints table with new fields
ALTER TABLE complaints ADD COLUMN user_email TEXT(255);
ALTER TABLE complaints ADD COLUMN complainant_contact TEXT(50);
ALTER TABLE complaints ADD COLUMN complainant_address MEMO;
ALTER TABLE complaints ADD COLUMN incident_category TEXT(100);
ALTER TABLE complaints ADD COLUMN incident_date TEXT(50);
ALTER TABLE complaints ADD COLUMN priority TEXT(20);
ALTER TABLE complaints ADD COLUMN evidence_photos TEXT(1000);
ALTER TABLE complaints ADD COLUMN witness_info MEMO;
ALTER TABLE complaints ADD COLUMN resolution MEMO;
ALTER TABLE complaints ADD COLUMN assigned_to TEXT(255);
ALTER TABLE complaints ADD COLUMN resolved_date TEXT(50);
ALTER TABLE complaints ADD COLUMN is_anonymous YESNO;
ALTER TABLE complaints ADD COLUMN created_at TEXT(50);
ALTER TABLE complaints ADD COLUMN updated_at TEXT(50);

-- Update announcements table with new fields
ALTER TABLE announcements ADD COLUMN summary TEXT(500);
ALTER TABLE announcements ADD COLUMN target_audience TEXT(100);
ALTER TABLE announcements ADD COLUMN expiry_date TEXT(50);
ALTER TABLE announcements ADD COLUMN is_active YESNO;
ALTER TABLE announcements ADD COLUMN is_featured YESNO;
ALTER TABLE announcements ADD COLUMN image_path TEXT(500);
ALTER TABLE announcements ADD COLUMN attachment_path TEXT(500);
ALTER TABLE announcements ADD COLUMN view_count INTEGER;
ALTER TABLE announcements ADD COLUMN created_at TEXT(50);
ALTER TABLE announcements ADD COLUMN updated_at TEXT(50);

-- Update notifications table with new fields
ALTER TABLE notifications ADD COLUMN title TEXT(255);
ALTER TABLE notifications ADD COLUMN reference_type TEXT(50);
ALTER TABLE notifications ADD COLUMN priority TEXT(20);
ALTER TABLE notifications ADD COLUMN action_url TEXT(500);
ALTER TABLE notifications ADD COLUMN read_at TEXT(50);

-- =====================================================
-- 2. CREATE NEW TABLES
-- =====================================================

-- Create document_types table
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

-- Create audit_logs table
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

-- Create system_settings table
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

-- Create barangay_officials table
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
-- 3. UPDATE EXISTING DATA
-- =====================================================

-- Set default values for new user fields
UPDATE users SET is_active = True WHERE is_active IS NULL;
UPDATE users SET updated_at = created_at WHERE updated_at IS NULL;

-- Set default values for new document request fields
UPDATE document_requests SET priority = 'Medium' WHERE priority IS NULL;
UPDATE document_requests SET is_paid = False WHERE is_paid IS NULL;
UPDATE document_requests SET created_at = date_requested WHERE created_at IS NULL;
UPDATE document_requests SET updated_at = date_requested WHERE updated_at IS NULL;

-- Set default values for new payment fields
UPDATE payments SET archived = False WHERE archived IS NULL;
UPDATE payments SET created_at = date_created WHERE created_at IS NULL;
UPDATE payments SET updated_at = date_created WHERE updated_at IS NULL;

-- Set default values for new complaint fields
UPDATE complaints SET priority = 'Medium' WHERE priority IS NULL;
UPDATE complaints SET is_anonymous = False WHERE is_anonymous IS NULL;
UPDATE complaints SET created_at = date_filed WHERE created_at IS NULL;
UPDATE complaints SET updated_at = date_filed WHERE updated_at IS NULL;

-- Set default values for new announcement fields
UPDATE announcements SET is_active = True WHERE is_active IS NULL;
UPDATE announcements SET is_featured = False WHERE is_featured IS NULL;
UPDATE announcements SET view_count = 0 WHERE view_count IS NULL;
UPDATE announcements SET created_at = date_posted WHERE created_at IS NULL;
UPDATE announcements SET updated_at = date_posted WHERE updated_at IS NULL;

-- =====================================================
-- 4. INSERT INITIAL DATA FOR NEW TABLES
-- =====================================================

-- Insert document types
INSERT INTO document_types (type_name, description, base_fee, processing_days, requirements, is_active, created_at) VALUES
('Barangay Clearance', 'Certificate of good moral character and residence', 100.00, 3, 'Valid ID, Proof of Residency, Cedula', True, '2024-06-01 00:00:00'),
('Certificate of Residency', 'Proof of residence in the barangay', 50.00, 2, 'Valid ID, Utility Bill, Barangay ID', True, '2024-06-01 00:00:00'),
('Certificate of Indigency', 'Certificate for low-income families', 30.00, 5, 'Valid ID, Proof of Income, Family Members List', True, '2024-06-01 00:00:00'),
('Business Permit', 'Permit to operate business within barangay', 500.00, 7, 'Business Registration, Tax Clearance, Location Clearance', True, '2024-06-01 00:00:00');

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
-- 5. DATA CLEANUP AND OPTIMIZATION
-- =====================================================

-- Update payment table to link with document requests
UPDATE payments 
SET document_request_id = ref_number 
WHERE document_request_id IS NULL AND ref_number IS NOT NULL;

-- Update payment table to set user_email based on document requests
UPDATE payments 
SET user_email = (
    SELECT dr.user_email 
    FROM document_requests dr 
    WHERE dr.request_id = payments.reference_number
) 
WHERE user_email IS NULL;

-- Set processing fees for existing document requests based on document type
UPDATE document_requests 
SET processing_fee = (
    CASE 
        WHEN document_type = 'Barangay Clearance' THEN 100.00
        WHEN document_type = 'Certificate of Residency' THEN 50.00
        WHEN document_type = 'Certificate of Indigency' THEN 30.00
        WHEN document_type = 'Business Permit' THEN 500.00
        ELSE 50.00
    END
)
WHERE processing_fee IS NULL;

-- =====================================================
-- 6. VERIFICATION QUERIES
-- =====================================================

-- Run these queries to verify the migration was successful:

-- Check if all tables exist
-- SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;

-- Check user table structure
-- PRAGMA table_info(users);

-- Check document_requests table structure
-- PRAGMA table_info(document_requests);

-- Check if data was migrated correctly
-- SELECT COUNT(*) as user_count FROM users;
-- SELECT COUNT(*) as document_count FROM document_requests;
-- SELECT COUNT(*) as payment_count FROM payments;
-- SELECT COUNT(*) as announcement_count FROM announcements;

-- =====================================================
-- MIGRATION COMPLETE
-- =====================================================

-- The migration is now complete. Your database has been updated with:
-- 1. Enhanced table structures with new fields
-- 2. New tables for document types, audit logs, system settings, and officials
-- 3. Updated existing data with default values
-- 4. New reference data for document types and system settings
--
-- Next steps:
-- 1. Test your application to ensure everything works correctly
-- 2. Update any hardcoded references to old field names in your code
-- 3. Consider creating regular backups of your enhanced database
-- 4. Review and customize the system settings as needed for your barangay