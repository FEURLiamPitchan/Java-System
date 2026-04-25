-- Simple Database Update for Document Requests
-- This script focuses only on the document_requests table for the three document types

-- =====================================================
-- DOCUMENT REQUESTS TABLE - SIMPLIFIED
-- =====================================================

-- Drop existing table if you want to recreate it
-- DROP TABLE document_requests;

-- Create document_requests table with fields for all three document types
CREATE TABLE document_requests (
    id AUTOINCREMENT PRIMARY KEY,
    request_id TEXT(255) UNIQUE,
    user_email TEXT(255),
    document_type TEXT(255),
    
    -- Common fields for all document types
    full_name TEXT(255),
    age INTEGER,
    gender TEXT(50),
    birth_place TEXT(255),
    birth_date TEXT(50),
    civil_status TEXT(50),
    address MEMO,
    years_residency INTEGER,
    contact_number TEXT(50),
    email_address TEXT(255),
    purpose TEXT(255),
    
    -- Certificate of Indigency specific fields
    occupation TEXT(255),
    head_of_family TEXT(255),
    family_members INTEGER,
    monthly_income CURRENCY,
    income_source TEXT(255),
    
    -- File upload fields
    valid_id_image TEXT(500),          -- For Barangay Clearance
    proof_of_residency TEXT(500),      -- For Certificate of Residency and Indigency
    proof_of_income TEXT(500),         -- For Certificate of Indigency
    
    -- Status and dates
    status TEXT(50),
    date_requested TEXT(50),
    date_completed TEXT(50)
);

-- =====================================================
-- DOCUMENT TYPE REQUIREMENTS
-- =====================================================

-- Barangay Clearance Requirements:
-- - Full name, Age, Gender, Birth Place, Birth Date, Civil Status
-- - Address, Years Residency, Contact Number, Email Address, Purpose
-- - Valid ID Image (required upload)

-- Certificate of Residency Requirements:
-- - Full name, Age, Gender, Birth Place, Birth Date, Civil Status
-- - Address, Length of Residency, Contact Number, Email Address, Purpose
-- - Proof of Residency (required upload - utility bill, barangay ID, etc.)

-- Certificate of Indigency Requirements:
-- - Full name, Age, Gender, Birth Date, Civil Status, Occupation
-- - Address, Years of Residency, Name of Head Family, Number of Family Members
-- - Family Monthly Income, Source of Income, Contact Number, Email Address, Purpose
-- - Proof of Income (required upload - payslip, employment certificate, etc.)
-- - Proof of Residency (required upload - barangay ID, utility bill, etc.)

-- =====================================================
-- SAMPLE DATA
-- =====================================================

-- Insert sample document requests
INSERT INTO document_requests (
    request_id, user_email, document_type, full_name, age, gender, 
    birth_place, birth_date, civil_status, address, years_residency, 
    contact_number, email_address, purpose, valid_id_image, status, date_requested
) VALUES (
    'REQ-2024-001', 'resident@email.com', 'Barangay Clearance', 'Maria Santos', 34, 'Female',
    'Manila', '1990-05-20', 'Single', '123 Main Street, San Isidro', 5,
    '09987654321', 'resident@email.com', 'Employment', 'uploads/valid_id/id_001.jpg', 'Pending', '2024-06-15'
);

INSERT INTO document_requests (
    request_id, user_email, document_type, full_name, age, gender, 
    birth_place, birth_date, civil_status, address, years_residency, 
    contact_number, email_address, purpose, proof_of_residency, status, date_requested
) VALUES (
    'REQ-2024-002', 'john.doe@email.com', 'Certificate of Residency', 'John Doe', 39, 'Male',
    'Quezon City', '1985-12-10', 'Married', '456 Oak Avenue, San Isidro', 10,
    '09111222333', 'john.doe@email.com', 'School Requirements', 'uploads/proof_residency/utility_002.pdf', 'In Progress', '2024-06-16'
);

INSERT INTO document_requests (
    request_id, user_email, document_type, full_name, age, gender, 
    birth_date, civil_status, occupation, address, years_residency, 
    head_of_family, family_members, monthly_income, income_source,
    contact_number, email_address, purpose, proof_of_income, proof_of_residency, 
    status, date_requested
) VALUES (
    'REQ-2024-003', 'maria.cruz@email.com', 'Certificate of Indigency', 'Maria Cruz', 28, 'Female',
    '1996-03-15', 'Single', 'Unemployed', '789 Pine Street, San Isidro', 3,
    'Pedro Cruz', 4, 8000.00, 'Family Support',
    '09555666777', 'maria.cruz@email.com', 'Medical Assistance', 'uploads/proof_income/support_003.jpg', 'uploads/proof_residency/barangay_id_003.jpg', 'Pending', '2024-06-17'
);

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Check all document requests
-- SELECT * FROM document_requests ORDER BY date_requested DESC;

-- Check by document type
-- SELECT * FROM document_requests WHERE document_type = 'Barangay Clearance';
-- SELECT * FROM document_requests WHERE document_type = 'Certificate of Residency';
-- SELECT * FROM document_requests WHERE document_type = 'Certificate of Indigency';

-- Check by status
-- SELECT * FROM document_requests WHERE status = 'Pending';

-- Count requests by type
-- SELECT document_type, COUNT(*) as count FROM document_requests GROUP BY document_type;