-- Drop existing table
DROP TABLE document_requests;

-- Create new document_requests table with clean structure
CREATE TABLE document_requests (
    id AUTOINCREMENT PRIMARY KEY,
    request_id TEXT(255),
    document_type TEXT(255),
    full_name TEXT(255),
    address MEMO,
    birthdate TEXT(50),
    civil_status TEXT(50),
    purpose TEXT(255),
    years_of_residency TEXT(50),
    status TEXT(50),
    date_requested TEXT(50),
    user_email TEXT(255)
);
