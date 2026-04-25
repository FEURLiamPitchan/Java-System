# Database Foreign Key Relationships - barangay.accdb

## Database Location
**Path:** `C:\Projects 2\dashboard_resident\barangay.accdb`  
**Type:** Microsoft Access Database (.accdb)  
**Driver:** UCanAccess JDBC Driver

---

## Foreign Key Relationships

### 1. **users** (Parent Table)
Primary Key: `email` (TEXT)

**Child Tables:**
- `document_requests.user_email` → `users.email`
- `payments.user_email` → `users.email`
- `complaints.user_email` → `users.email`
- `notifications.user_email` → `users.email`
- `settings.user_email` → `users.email`

---

### 2. **document_requests** (Child of users, Parent of payments)
Primary Key: `request_id` (TEXT)  
Foreign Key: `user_email` → `users.email`

**Child Tables:**
- `payments.document_request_id` → `document_requests.request_id`
- `payments.reference_number` → `document_requests.request_id`

---

### 3. **payments** (Child of users and document_requests)
Primary Key: `payment_id` (TEXT)  
Foreign Keys:
- `user_email` → `users.email`
- `document_request_id` → `document_requests.request_id`
- `reference_number` → `document_requests.request_id`

---

### 4. **complaints** (Child of users)
Primary Key: `complaint_id` (TEXT)  
Foreign Key: `user_email` → `users.email`

---

### 5. **notifications** (Child of users)
Primary Key: `notif_id` (AUTOINCREMENT)  
Foreign Key: `user_email` → `users.email`

**Reference Fields:**
- `reference_id` can point to:
  - `document_requests.request_id`
  - `complaints.complaint_id`
  - `announcements.announcement_id`

---

### 6. **settings** (Child of users)
Primary Key: `id` (AUTOINCREMENT)  
Foreign Key: `user_email` → `users.email` (UNIQUE)

---

## SQL JOIN Examples Used in Code

### Example 1: MyProfileController - User Profile with Document Count
```sql
SELECT u.*, COUNT(dr.id) as total_requests 
FROM users u 
LEFT JOIN document_requests dr ON u.email = dr.user_email 
WHERE u.email = ? 
GROUP BY u.id, u.email, u.password, u.role, u.first_name, u.last_name, 
         u.contact_number, u.address, u.date_of_birth, u.gender, 
         u.civil_status, u.occupation, u.profile_picture, u.is_active, 
         u.created_at, u.updated_at
```

**Purpose:** Loads user profile and counts total document requests  
**FK Used:** `document_requests.user_email` → `users.email`

---

### Example 2: MyDocumentsController - User's Document Requests
```sql
SELECT * FROM document_requests 
WHERE user_email = ? OR user_email IS NULL 
ORDER BY date_requested DESC
```

**Purpose:** Retrieves all document requests for logged-in user  
**FK Used:** `document_requests.user_email` → `users.email`

---

### Example 3: ResidentPaymentsController - User's Payments
```sql
SELECT * FROM payments 
WHERE user_email = ? 
ORDER BY ID DESC
```

**Purpose:** Retrieves all payment records for logged-in user  
**FK Used:** `payments.user_email` → `users.email`

---

### Example 4: Complaints_ResidentController - User's Complaints
```sql
SELECT * FROM complaints 
WHERE user_email = ? 
ORDER BY id DESC
```

**Purpose:** Retrieves all complaints filed by logged-in user  
**FK Used:** `complaints.user_email` → `users.email`

---

### Example 5: ResidentNotifications - User's Notifications
```sql
SELECT notif_id, type, message, reference_id, is_read, created_at 
FROM notifications 
WHERE user_email = ? 
ORDER BY notif_id DESC
```

**Purpose:** Retrieves all notifications for logged-in user  
**FK Used:** `notifications.user_email` → `users.email`

---

## Advanced JOIN Query Examples

### Get User with All Related Data
```sql
SELECT 
    u.email,
    u.first_name,
    u.last_name,
    COUNT(DISTINCT dr.id) as total_documents,
    COUNT(DISTINCT p.id) as total_payments,
    COUNT(DISTINCT c.id) as total_complaints,
    COUNT(DISTINCT n.notif_id) as total_notifications
FROM users u
LEFT JOIN document_requests dr ON u.email = dr.user_email
LEFT JOIN payments p ON u.email = p.user_email
LEFT JOIN complaints c ON u.email = c.user_email
LEFT JOIN notifications n ON u.email = n.user_email
WHERE u.email = ?
GROUP BY u.email, u.first_name, u.last_name
```

---

### Get Document Request with Payment Status
```sql
SELECT 
    dr.request_id,
    dr.document_type,
    dr.status as document_status,
    dr.date_requested,
    p.payment_id,
    p.amount,
    p.status as payment_status,
    p.payment_date,
    u.first_name,
    u.last_name,
    u.email
FROM document_requests dr
LEFT JOIN payments p ON dr.request_id = p.document_request_id
INNER JOIN users u ON dr.user_email = u.email
WHERE dr.user_email = ?
ORDER BY dr.date_requested DESC
```

---

### Get User's Complete Activity Timeline
```sql
SELECT 
    'Document' as activity_type,
    dr.request_id as reference,
    dr.document_type as description,
    dr.date_requested as activity_date
FROM document_requests dr
WHERE dr.user_email = ?

UNION ALL

SELECT 
    'Payment' as activity_type,
    p.payment_id as reference,
    p.description as description,
    p.payment_date as activity_date
FROM payments p
WHERE p.user_email = ?

UNION ALL

SELECT 
    'Complaint' as activity_type,
    c.complaint_id as reference,
    c.incident_type as description,
    c.date_filed as activity_date
FROM complaints c
WHERE c.user_email = ?

ORDER BY activity_date DESC
```

---

## Database Schema Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                         users (PARENT)                       │
│  PK: email                                                   │
│  - id, password, role, first_name, last_name                │
│  - contact_number, address, date_of_birth, gender           │
│  - civil_status, occupation, profile_picture                │
│  - is_active, created_at, updated_at                        │
└────────────────┬────────────────────────────────────────────┘
                 │
                 │ FK: user_email
                 │
    ┌────────────┼────────────┬────────────┬────────────┐
    │            │            │            │            │
    ▼            ▼            ▼            ▼            ▼
┌─────────┐ ┌─────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐
│document_│ │payments │ │complaints│ │notifica- │ │settings │
│requests │ │         │ │          │ │tions     │ │         │
└────┬────┘ └─────────┘ └──────────┘ └──────────┘ └─────────┘
     │
     │ FK: document_request_id
     │
     ▼
┌─────────┐
│payments │
│(also)   │
└─────────┘
```

---

## How to Use in Your Code

### 1. Load User Profile with Related Data
```java
String sql = "SELECT u.*, COUNT(dr.id) as total_requests " +
             "FROM users u " +
             "LEFT JOIN document_requests dr ON u.email = dr.user_email " +
             "WHERE u.email = ? " +
             "GROUP BY u.email";
```

### 2. Update User Profile
```java
String sql = "UPDATE users SET first_name = ?, last_name = ?, " +
             "contact_number = ?, address = ?, gender = ?, " +
             "date_of_birth = ?, updated_at = ? " +
             "WHERE email = ?";
```

### 3. Insert Document Request (Creates FK relationship)
```java
String sql = "INSERT INTO document_requests " +
             "(request_id, user_email, document_type, ...) " +
             "VALUES (?, ?, ?, ...)";
// user_email creates FK link to users table
```

### 4. Query with Multiple JOINs
```java
String sql = "SELECT dr.*, p.status as payment_status, u.first_name " +
             "FROM document_requests dr " +
             "LEFT JOIN payments p ON dr.request_id = p.document_request_id " +
             "INNER JOIN users u ON dr.user_email = u.email " +
             "WHERE dr.user_email = ?";
```

---

## Important Notes

1. **Cascade Behavior**: MS Access doesn't enforce FK constraints like MySQL/PostgreSQL, so you must handle cascading deletes/updates in application code

2. **Email as FK**: Using `email` as FK is acceptable but consider using numeric `user_id` for better performance in production

3. **NULL Values**: Use `LEFT JOIN` when child records might not exist (e.g., user with no documents)

4. **GROUP BY**: When using aggregate functions (COUNT, SUM), include all non-aggregated columns in GROUP BY

5. **Performance**: Index the `user_email` column in all child tables for faster JOIN queries

---

## Testing FK Relationships

Run this query to verify all relationships:

```sql
SELECT 
    u.email,
    u.first_name,
    u.last_name,
    (SELECT COUNT(*) FROM document_requests WHERE user_email = u.email) as docs,
    (SELECT COUNT(*) FROM payments WHERE user_email = u.email) as payments,
    (SELECT COUNT(*) FROM complaints WHERE user_email = u.email) as complaints,
    (SELECT COUNT(*) FROM notifications WHERE user_email = u.email) as notifs
FROM users u
WHERE u.email = 'resident@email.com'
```

Expected output for sample data:
- docs: 2
- payments: 1
- complaints: 0
- notifs: varies
