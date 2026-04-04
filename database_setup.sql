-- Database Table Structure for Barangay Management System
-- Create this table in your barangay.accdb database

-- Users Table (for login/register and basic profile)
CREATE TABLE users (
    id AUTOINCREMENT PRIMARY KEY,
    email TEXT(100) NOT NULL UNIQUE,
    password TEXT(100) NOT NULL,
    role TEXT(20) NOT NULL,
    created_at TEXT(20) DEFAULT NULL
);

-- Document Requests Table
CREATE TABLE document_requests (
    id AUTOINCREMENT PRIMARY KEY,
    request_id TEXT(50) NOT NULL UNIQUE,
    document_type TEXT(100) NOT NULL,
    full_name TEXT(100) NOT NULL,
    address TEXT(255) NOT NULL,
    contact_number TEXT(20) NOT NULL,
    purpose TEXT(255) NOT NULL,
    notes TEXT(500),
    status TEXT(50) DEFAULT 'Pending',
    date_requested TEXT(20) NOT NULL,
    created_at TEXT(20) DEFAULT NULL
);

-- Announcements Table
CREATE TABLE announcements (
    id AUTOINCREMENT PRIMARY KEY,
    announcement_id TEXT(50) NOT NULL UNIQUE,
    title TEXT(200) NOT NULL,
    content MEMO,
    priority TEXT(20) NOT NULL,
    category TEXT(50) NOT NULL,
    posted_by TEXT(100) NOT NULL,
    date_posted TEXT(20) NOT NULL
);

-- Sample data
-- Admin account
INSERT INTO users (email, password, role, created_at) VALUES ('admin@barangay.com', 'admin123', 'admin', '2024-01-01');

-- Resident account
INSERT INTO users (email, password, role, created_at) VALUES ('resident@email.com', 'resident123', 'resident', '2024-01-01');

-- Sample document requests
INSERT INTO document_requests (request_id, document_type, full_name, address, contact_number, purpose, notes, status, date_requested, created_at) 
VALUES ('#BR-2024-001', 'Barangay Clearance', 'Juan Dela Cruz', '123 Main St, Barangay San Isidro', '09123456789', 'Employment', 'Urgent request', 'In Progress', '2024-06-13', '2024-06-13');

INSERT INTO document_requests (request_id, document_type, full_name, address, contact_number, purpose, notes, status, date_requested, created_at) 
VALUES ('#BR-2024-002', 'Certificate of Residency', 'Maria Santos', '456 Oak Ave, Barangay San Isidro', '09987654321', 'School Requirements', '', 'Ready', '2024-06-12', '2024-06-12');

INSERT INTO document_requests (request_id, document_type, full_name, address, contact_number, purpose, notes, status, date_requested, created_at) 
VALUES ('#BR-2024-003', 'Certificate of Indigency', 'Pedro Reyes', '789 Pine St, Barangay San Isidro', '09111222333', 'Medical Assistance', 'For hospital requirements', 'Released', '2024-06-10', '2024-06-10');

-- Sample announcements
INSERT INTO announcements (announcement_id, title, content, priority, category, posted_by, date_posted)
VALUES ('ANN-0001', 'Community Clean-Up Drive', 'Join us this Saturday for our monthly community clean-up. Bring your own cleaning materials.', 'Normal', 'Events', 'Barangay Captain', '2024-06-15 10:00:00');

INSERT INTO announcements (announcement_id, title, content, priority, category, posted_by, date_posted)
VALUES ('ANN-0002', 'Typhoon Warning', 'Typhoon approaching. Please prepare emergency kits and stay indoors.', 'Emergency', 'Safety & Security', 'Disaster Response Team', '2024-06-14 08:00:00');

INSERT INTO announcements (announcement_id, title, content, priority, category, posted_by, date_posted)
VALUES ('ANN-0003', 'Free Medical Check-up', 'Free medical check-up and consultation available at the barangay health center this Friday.', 'Urgent', 'Health', 'Health Officer', '2024-06-13 14:00:00');
