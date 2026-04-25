# Barangay Management System Database Documentation

## Database Overview
This document describes the enhanced database structure for the Barangay San Isidro Management System. The database is designed to handle document requests, payments, complaints, announcements, and user management.

## Database Tables

### 1. users
**Purpose**: Store user account information for both residents and administrators
```sql
CREATE TABLE users (
    id AUTOINCREMENT PRIMARY KEY,
    email TEXT(255) UNIQUE,
    password TEXT(255),
    role TEXT(50),                    -- 'admin' or 'resident'
    first_name TEXT(100),
    last_name TEXT(100),
    contact_number TEXT(20),
    address MEMO,
    date_of_birth TEXT(50),
    gender TEXT(20),                  -- 'Male', 'Female', 'Other'
    civil_status TEXT(50),            -- 'Single', 'Married', 'Divorced', 'Widowed'
    occupation TEXT(100),
    profile_picture TEXT(500),        -- Path to profile image
    is_active YESNO,                  -- Account status
    created_at TEXT(50),
    updated_at TEXT(50)
);
```

### 2. document_types
**Purpose**: Define available document types and their properties
```sql
CREATE TABLE document_types (
    id AUTOINCREMENT PRIMARY KEY,
    type_name TEXT(255),              -- 'Barangay Clearance', 'Certificate of Residency', etc.
    description MEMO,                 -- Detailed description
    base_fee CURRENCY,                -- Standard processing fee
    processing_days INTEGER,          -- Expected processing time
    requirements MEMO,                -- Required documents/information
    is_active YESNO,                  -- Whether this document type is available
    created_at TEXT(50)
);
```

### 3. document_requests
**Purpose**: Store all document requests from residents
```sql
CREATE TABLE document_requests (
    id AUTOINCREMENT PRIMARY KEY,
    request_id TEXT(255) UNIQUE,      -- Format: REQ-YYYY-NNNN
    user_email TEXT(255),             -- Links to users table
    document_type TEXT(255),          -- Type of document requested
    
    -- Personal Information
    full_name TEXT(255),
    age INTEGER,
    gender TEXT(50),
    birth_place TEXT(255),
    birth_date TEXT(50),
    civil_status TEXT(50),
    occupation TEXT(255),
    address MEMO,
    years_residency INTEGER,
    contact_number TEXT(50),
    email_address TEXT(255),
    
    -- Family Information (for Indigency Certificate)
    head_of_family TEXT(255),
    family_members INTEGER,
    monthly_income CURRENCY,
    income_source TEXT(255),
    
    -- Business Information (for Business Permit)
    business_name TEXT(255),
    business_type TEXT(255),
    business_address MEMO,
    
    -- Request Details
    purpose TEXT(255),                -- Purpose of the document
    
    -- File Attachments
    proof_of_residency TEXT(500),     -- Path to uploaded file
    proof_of_income TEXT(500),        -- Path to uploaded file
    business_permit_copy TEXT(500),   -- Path to uploaded file
    other_documents TEXT(500),        -- Path to other supporting documents
    
    -- Processing Information
    notes MEMO,                       -- Resident's additional notes
    admin_notes MEMO,                 -- Admin's processing notes
    status TEXT(50),                  -- 'Pending', 'In Progress', 'Ready', 'Released', 'Rejected'
    priority TEXT(20),                -- 'Low', 'Medium', 'High', 'Urgent'
    assigned_to TEXT(255),            -- Admin handling the request
    
    -- Dates
    date_requested TEXT(50),
    date_approved TEXT(50),
    date_completed TEXT(50),
    date_released TEXT(50),
    
    -- Payment Information
    processing_fee CURRENCY,
    is_paid YESNO,
    payment_reference TEXT(255),
    
    -- Generated Document
    document_path TEXT(500),          -- Path to generated document
    
    created_at TEXT(50),
    updated_at TEXT(50)
);
```

### 4. payments
**Purpose**: Track all payment transactions
```sql
CREATE TABLE payments (
    id AUTOINCREMENT PRIMARY KEY,
    payment_id TEXT(255) UNIQUE,      -- Format: PAY-YYYY-NNNN
    user_email TEXT(255),             -- Links to users table
    reference_number TEXT(255),       -- Reference to document request
    document_request_id TEXT(255),    -- Links to document_requests table
    payment_type TEXT(100),           -- 'Document Fee', 'Penalty', 'Service Fee'
    description TEXT(255),            -- Payment description
    amount CURRENCY,                  -- Payment amount
    payment_method TEXT(50),          -- 'Cash', 'Online', 'Bank Transfer'
    transaction_id TEXT(255),         -- External transaction ID (PayMongo, etc.)
    paymongo_link_id TEXT(255),       -- PayMongo payment link ID
    status TEXT(50),                  -- 'Pending', 'Paid', 'Failed', 'Refunded'
    payment_date TEXT(50),            -- When payment was made
    due_date TEXT(50),                -- Payment due date
    receipt_number TEXT(255),         -- Official receipt number
    receipt_path TEXT(500),           -- Path to receipt file
    notes MEMO,                       -- Additional payment notes
    processed_by TEXT(255),           -- Admin who processed payment
    archived YESNO,                   -- Whether payment is archived
    created_at TEXT(50),
    updated_at TEXT(50)
);
```

### 5. complaints
**Purpose**: Handle resident complaints and issues
```sql
CREATE TABLE complaints (
    id AUTOINCREMENT PRIMARY KEY,
    complaint_id TEXT(255) UNIQUE,    -- Format: CMP-YYYY-NNNN
    user_email TEXT(255),             -- Links to users table
    complainant_name TEXT(255),
    complainant_contact TEXT(50),
    complainant_address MEMO,
    incident_type TEXT(255),          -- Type of complaint
    incident_category TEXT(100),      -- Category classification
    location TEXT(255),               -- Where incident occurred
    incident_date TEXT(50),           -- When incident occurred
    date_filed TEXT(50),              -- When complaint was filed
    status TEXT(50),                  -- 'Open', 'In Progress', 'Resolved', 'Closed'
    priority TEXT(20),                -- 'Low', 'Medium', 'High', 'Critical'
    incident_details MEMO,            -- Detailed description
    evidence_photos TEXT(1000),       -- Paths to uploaded photos (comma-separated)
    witness_info MEMO,                -- Witness information
    admin_response MEMO,              -- Official response
    resolution MEMO,                  -- How complaint was resolved
    assigned_to TEXT(255),            -- Admin handling complaint
    resolved_date TEXT(50),           -- When complaint was resolved
    is_anonymous YESNO,               -- Whether complaint is anonymous
    is_read YESNO,                    -- Whether admin has read complaint
    created_at TEXT(50),
    updated_at TEXT(50)
);
```

### 6. announcements
**Purpose**: Manage barangay announcements and news
```sql
CREATE TABLE announcements (
    id AUTOINCREMENT PRIMARY KEY,
    announcement_id TEXT(255) UNIQUE, -- Format: ANN-YYYY-NNNN
    title TEXT(255),                  -- Announcement title
    content MEMO,                     -- Full announcement content
    summary TEXT(500),                -- Brief summary
    priority TEXT(50),                -- 'Low', 'Medium', 'High', 'Critical'
    category TEXT(100),               -- 'Health', 'Government', 'Events', 'Emergency'
    target_audience TEXT(100),        -- 'All Residents', 'Business Owners', 'Senior Citizens'
    posted_by TEXT(255),              -- Admin who posted
    date_posted TEXT(50),             -- Publication date
    expiry_date TEXT(50),             -- When announcement expires
    is_active YESNO,                  -- Whether announcement is visible
    is_featured YESNO,                -- Whether to feature prominently
    image_path TEXT(500),             -- Path to announcement image
    attachment_path TEXT(500),        -- Path to attachment file
    view_count INTEGER,               -- Number of views
    created_at TEXT(50),
    updated_at TEXT(50)
);
```

### 7. notifications
**Purpose**: System notifications for users
```sql
CREATE TABLE notifications (
    notif_id AUTOINCREMENT PRIMARY KEY,
    user_email TEXT(255),             -- Recipient email
    type TEXT(50),                    -- 'document', 'payment', 'complaint', 'announcement'
    title TEXT(255),                  -- Notification title
    message MEMO,                     -- Notification message
    reference_id TEXT(255),           -- ID of related record
    reference_type TEXT(50),          -- Type of referenced record
    is_read TEXT(10),                 -- 'true' or 'false'
    priority TEXT(20),                -- 'Low', 'Medium', 'High'
    action_url TEXT(500),             -- URL for notification action
    created_at TEXT(50),              -- When notification was created
    read_at TEXT(50)                  -- When notification was read
);
```

### 8. audit_logs
**Purpose**: Track system activities for security and compliance
```sql
CREATE TABLE audit_logs (
    id AUTOINCREMENT PRIMARY KEY,
    user_email TEXT(255),             -- User who performed action
    action TEXT(255),                 -- Action performed
    table_name TEXT(100),             -- Table affected
    record_id TEXT(255),              -- ID of affected record
    old_values MEMO,                  -- Previous values (JSON)
    new_values MEMO,                  -- New values (JSON)
    ip_address TEXT(50),              -- User's IP address
    user_agent TEXT(500),             -- Browser/client information
    created_at TEXT(50)               -- When action occurred
);
```

### 9. system_settings
**Purpose**: Store system configuration and settings
```sql
CREATE TABLE system_settings (
    id AUTOINCREMENT PRIMARY KEY,
    setting_key TEXT(255) UNIQUE,     -- Setting identifier
    setting_value MEMO,               -- Setting value
    setting_type TEXT(50),            -- 'text', 'number', 'boolean', 'json'
    description TEXT(500),            -- Setting description
    is_public YESNO,                  -- Whether setting is publicly visible
    updated_by TEXT(255),             -- Who last updated setting
    updated_at TEXT(50)               -- When setting was last updated
);
```

### 10. barangay_officials
**Purpose**: Information about barangay officials
```sql
CREATE TABLE barangay_officials (
    id AUTOINCREMENT PRIMARY KEY,
    position TEXT(255),               -- Official position/title
    full_name TEXT(255),              -- Official's full name
    contact_number TEXT(50),          -- Contact number
    email TEXT(255),                  -- Email address
    address MEMO,                     -- Address
    term_start TEXT(50),              -- Start of term
    term_end TEXT(50),                -- End of term
    is_active YESNO,                  -- Whether currently serving
    photo_path TEXT(500),             -- Path to official's photo
    bio MEMO,                         -- Biography/description
    created_at TEXT(50),
    updated_at TEXT(50)
);
```

## Document Types Supported

### 1. Barangay Clearance
- **Fee**: ₱100.00
- **Processing Time**: 3 days
- **Requirements**: Valid ID, Proof of Residency, Cedula
- **Fields**: Basic personal information, purpose

### 2. Certificate of Residency
- **Fee**: ₱50.00
- **Processing Time**: 2 days
- **Requirements**: Valid ID, Utility Bill, Barangay ID
- **Fields**: Personal information, years of residency, proof of residency upload

### 3. Certificate of Indigency
- **Fee**: ₱30.00
- **Processing Time**: 5 days
- **Requirements**: Valid ID, Proof of Income, Family Members List
- **Fields**: Personal information, family details, income information, dual file uploads

### 4. Business Permit
- **Fee**: ₱500.00
- **Processing Time**: 7 days
- **Requirements**: Business Registration, Tax Clearance, Location Clearance
- **Fields**: Personal and business information, business details

## Status Workflow

### Document Request Status Flow
1. **Pending** → Initial submission
2. **In Progress** → Being reviewed/processed
3. **Ready** → Document completed, ready for pickup/payment
4. **Released** → Document released to resident
5. **Rejected** → Request rejected (with reason)

### Payment Status Flow
1. **Pending** → Payment required
2. **Paid** → Payment completed
3. **Failed** → Payment failed
4. **Refunded** → Payment refunded

### Complaint Status Flow
1. **Open** → New complaint filed
2. **In Progress** → Being investigated
3. **Resolved** → Issue resolved
4. **Closed** → Case closed

## Sample Data Included

The database includes sample data for:
- 3 user accounts (1 admin, 2 residents)
- 4 document types with fees and requirements
- 3 sample document requests in different statuses
- 2 sample payment records
- 2 sample announcements
- System settings for barangay information
- 3 barangay officials

## Security Features

1. **User Authentication**: Email-based login with role-based access
2. **Audit Logging**: All system activities are logged
3. **Data Validation**: Comprehensive input validation
4. **File Upload Security**: Secure file handling with proper directory structure
5. **Session Management**: Proper user session handling

## Integration Points

1. **PayMongo Integration**: Online payment processing
2. **File Storage**: Secure document and image storage
3. **Notification System**: Real-time user notifications
4. **Email System**: Automated email notifications (future enhancement)
5. **SMS Integration**: SMS notifications (future enhancement)

## Backup and Maintenance

1. **Regular Backups**: Automated database backups recommended
2. **Data Archiving**: Old records can be archived using the archived flags
3. **Performance Monitoring**: Monitor query performance and optimize as needed
4. **Data Cleanup**: Regular cleanup of temporary files and old notifications

This database structure provides a solid foundation for a comprehensive barangay management system with room for future enhancements and scalability.