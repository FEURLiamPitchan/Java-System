# QUICK REFERENCE: Correct SQL Queries for barangay.accdb

## USER OPERATIONS

### Login Query
```sql
SELECT id, email, password, role, full_name, status 
FROM users 
WHERE email = ?
```

### Get User by Email
```sql
SELECT u.id, u.email, u.role, u.full_name, u.status, u.date_created,
       r.age, r.address, r.gender, r.birth_place, r.birth_date, 
       r.civil_status, r.contact_number
FROM users u
LEFT JOIN residents r ON u.id = r.user_id
WHERE u.email = ?
```

### Register New User
```sql
-- Step 1: Insert into users table
INSERT INTO users (email, password, role, full_name, status, date_created)
VALUES (?, ?, 'resident', ?, 'active', ?)

-- Step 2: Get the user_id
SELECT id FROM users WHERE email = ?

-- Step 3: Insert into residents table
INSERT INTO residents (full_name, user_id, status, date_added)
VALUES (?, ?, 'active', ?)
```

---

## DOCUMENT REQUEST OPERATIONS

### Get All Document Requests for User
```sql
SELECT dr.id, dr.request_id, dr.document_type, dr.purpose, dr.status,
       dr.date_requested, dr.date_completed,
       r.full_name, r.age, r.gender, r.address, r.contact_number
FROM document_requests dr
INNER JOIN residents r ON dr.resident_id = r.id
INNER JOIN users u ON r.user_id = u.id
WHERE u.email = ?
ORDER BY dr.date_requested DESC
```

### Submit New Document Request
```sql
-- Step 1: Get resident_id from user email
SELECT r.id 
FROM residents r
INNER JOIN users u ON r.user_id = u.id
WHERE u.email = ?

-- Step 2: Insert document request
INSERT INTO document_requests (
    request_id, document_type, purpose, status, date_requested,
    years_residency, email_address, occupation,
    head_of_family, family_members, monthly_income, income_source,
    valid_id_path, proof_of_residency_path, proof_of_income_path,
    resident_id, requested_by, created_at
) VALUES (?, ?, ?, 'Pending', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
```

### Get Document Request Details
```sql
SELECT dr.*, r.full_name, r.age, r.gender, r.birth_place, r.birth_date,
       r.civil_status, r.address, r.contact_number
FROM document_requests dr
INNER JOIN residents r ON dr.resident_id = r.id
WHERE dr.request_id = ?
```

---

## PAYMENT OPERATIONS

### Get All Payments for User
```sql
SELECT p.id, p.payment_id, p.ref_number, p.payment_type, p.amount,
       p.status, p.date_created, p.request_id,
       dr.document_type
FROM payments p
LEFT JOIN document_requests dr ON p.request_id = dr.request_id
INNER JOIN residents r ON p.resident_id = r.id
INNER JOIN users u ON r.user_id = u.id
WHERE u.email = ?
ORDER BY p.date_created DESC
```

### Create Payment
```sql
-- Step 1: Get resident_id
SELECT r.id 
FROM residents r
INNER JOIN users u ON r.user_id = u.id
WHERE u.email = ?

-- Step 2: Insert payment
INSERT INTO payments (
    payment_id, ref_number, payment_type, amount, status,
    date_created, archived, resident_id, request_id
) VALUES (?, ?, ?, ?, 'Pending', ?, false, ?, ?)
```

### Update Payment Status
```sql
UPDATE payments 
SET status = ?, date_created = ?
WHERE payment_id = ?
```

---

## COMPLAINT OPERATIONS

### Get All Complaints for User
```sql
SELECT c.ID, c.complaint_id, c.complainant_name, c.incident_type,
       c.incident_details, c.location, c.incident_date, c.status,
       c.admin_response, c.date_filed, c.is_read
FROM complaints c
INNER JOIN residents r ON c.resident_id = r.id
INNER JOIN users u ON r.user_id = u.id
WHERE u.email = ?
ORDER BY c.date_filed DESC
```

### Submit New Complaint
```sql
-- Step 1: Get resident_id
SELECT r.id, r.full_name
FROM residents r
INNER JOIN users u ON r.user_id = u.id
WHERE u.email = ?

-- Step 2: Insert complaint
INSERT INTO complaints (
    complaint_id, complainant_name, incident_type, incident_details,
    location, incident_date, status, date_filed, is_read, resident_id
) VALUES (?, ?, ?, ?, ?, ?, 'Open', ?, false, ?)
```

---

## ANNOUNCEMENT OPERATIONS

### Get All Announcements
```sql
SELECT id, announcement_id, title, content, priority, category,
       posted_by, date_posted, created_at, updated_at
FROM announcements
ORDER BY date_posted DESC
```

### Get Announcements by Priority
```sql
SELECT id, announcement_id, title, content, priority, category,
       posted_by, date_posted
FROM announcements
WHERE priority = ?
ORDER BY date_posted DESC
```

---

## NOTIFICATION OPERATIONS

### Get Unread Notifications Count
```sql
SELECT COUNT(*) 
FROM notifications n
INNER JOIN users u ON n.user_id = u.id
WHERE u.email = ? AND n.is_read = false
```

### Get All Notifications for User
```sql
SELECT n.notif_id, n.type, n.message, n.reference_id, n.is_read, n.created_at
FROM notifications n
INNER JOIN users u ON n.user_id = u.id
WHERE u.email = ?
ORDER BY n.created_at DESC
```

### Get Unread Notifications Only
```sql
SELECT n.notif_id, n.type, n.message, n.reference_id, n.is_read, n.created_at
FROM notifications n
INNER JOIN users u ON n.user_id = u.id
WHERE u.email = ? AND n.is_read = false
ORDER BY n.created_at DESC
```

### Create Notification
```sql
-- Step 1: Get user_id from email
SELECT id FROM users WHERE email = ?

-- Step 2: Insert notification
INSERT INTO notifications (type, message, reference_id, is_read, created_at, user_id)
VALUES (?, ?, ?, false, ?, ?)
```

### Mark Notification as Read
```sql
UPDATE notifications 
SET is_read = true
WHERE notif_id = ?
```

---

## SETTINGS OPERATIONS

### Get User Settings
```sql
SELECT s.setting_id, s.dark_mode, s.font_size, s.notif_complaints,
       s.notif_payments, s.notif_announcements, s.base_population
FROM settings s
INNER JOIN users u ON s.user_id = u.id
WHERE u.email = ?
```

### Create Default Settings for New User
```sql
-- Step 1: Get user_id
SELECT id FROM users WHERE email = ?

-- Step 2: Insert settings
INSERT INTO settings (
    dark_mode, font_size, notif_complaints, notif_payments,
    notif_announcements, base_population, user_id
) VALUES ('false', 'medium', 'true', 'true', 'true', 0, ?)
```

### Update Settings
```sql
UPDATE settings 
SET dark_mode = ?, font_size = ?, notif_complaints = ?,
    notif_payments = ?, notif_announcements = ?
WHERE user_id = (SELECT id FROM users WHERE email = ?)
```

---

## PROFILE OPERATIONS

### Get Full User Profile
```sql
SELECT u.id, u.email, u.full_name, u.status, u.date_created, u.profile_picture,
       r.age, r.address, r.gender, r.birth_place, r.birth_date,
       r.civil_status, r.contact_number
FROM users u
LEFT JOIN residents r ON u.id = r.user_id
WHERE u.email = ?
```

### Update User Profile
```sql
-- Update users table
UPDATE users 
SET full_name = ?, profile_picture = ?
WHERE email = ?

-- Update residents table
UPDATE residents 
SET full_name = ?, age = ?, address = ?, gender = ?,
    birth_place = ?, birth_date = ?, civil_status = ?, contact_number = ?
WHERE user_id = (SELECT id FROM users WHERE email = ?)
```

### Change Password
```sql
UPDATE users 
SET password = ?
WHERE email = ?
```

---

## DASHBOARD STATISTICS

### Count Total Document Requests
```sql
SELECT COUNT(*) 
FROM document_requests dr
INNER JOIN residents r ON dr.resident_id = r.id
INNER JOIN users u ON r.user_id = u.id
WHERE u.email = ?
```

### Count Pending Document Requests
```sql
SELECT COUNT(*) 
FROM document_requests dr
INNER JOIN residents r ON dr.resident_id = r.id
INNER JOIN users u ON r.user_id = u.id
WHERE u.email = ? AND dr.status = 'Pending'
```

### Count Total Payments
```sql
SELECT COUNT(*) 
FROM payments p
INNER JOIN residents r ON p.resident_id = r.id
INNER JOIN users u ON r.user_id = u.id
WHERE u.email = ?
```

### Count Pending Payments
```sql
SELECT COUNT(*) 
FROM payments p
INNER JOIN residents r ON p.resident_id = r.id
INNER JOIN users u ON r.user_id = u.id
WHERE u.email = ? AND p.status = 'Pending'
```

---

## IMPORTANT NOTES

1. **Always use JOINs** to connect users → residents → other tables
2. **user_id is the bridge** between users and residents tables
3. **resident_id is the foreign key** in document_requests, payments, complaints
4. **Complaints table uses uppercase "ID"** for primary key column
5. **Boolean fields** (is_read, archived) use true/false, not 'true'/'false' strings
6. **Date fields** vary: some are DateTime, some are String - check actual type
7. **Always get user_id first** when inserting notifications or settings

---

## HELPER QUERIES

### Get user_id from email
```sql
SELECT id FROM users WHERE email = ?
```

### Get resident_id from email
```sql
SELECT r.id 
FROM residents r
INNER JOIN users u ON r.user_id = u.id
WHERE u.email = ?
```

### Check if resident record exists
```sql
SELECT COUNT(*) 
FROM residents r
INNER JOIN users u ON r.user_id = u.id
WHERE u.email = ?
```

### Create resident record if missing
```sql
INSERT INTO residents (full_name, user_id, status, date_added)
SELECT full_name, id, 'active', ?
FROM users
WHERE email = ? AND id NOT IN (SELECT user_id FROM residents WHERE user_id IS NOT NULL)
```

