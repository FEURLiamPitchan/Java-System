# Barangay Management System - Database Setup Guide

## Required Database: barangay.accdb

### Location
Place the `barangay.accdb` file in: `C:\barangay.accdb`

### Database Tables Required

#### 1. **users** Table
Stores user accounts for login/registration.

| Field Name | Data Type | Size | Properties |
|------------|-----------|------|------------|
| id | AutoNumber | - | Primary Key |
| email | Short Text | 100 | Required, Indexed (No Duplicates) |
| password | Short Text | 100 | Required |
| role | Short Text | 20 | Required (values: "admin" or "resident") |
| created_at | Short Text | 20 | Optional |

**Sample Data:**
```
email: admin@barangay.com, password: admin123, role: admin
email: resident@email.com, password: resident123, role: resident
```

---

#### 2. **document_requests** Table
Stores document requests from residents.

| Field Name | Data Type | Size | Properties |
|------------|-----------|------|------------|
| id | AutoNumber | - | Primary Key |
| request_id | Short Text | 50 | Required, Indexed (No Duplicates) |
| document_type | Short Text | 100 | Required |
| full_name | Short Text | 100 | Required |
| address | Short Text | 255 | Required |
| contact_number | Short Text | 20 | Required |
| purpose | Short Text | 255 | Required |
| notes | Long Text | - | Optional |
| status | Short Text | 50 | Default: "Pending" |
| date_requested | Short Text | 20 | Required |
| created_at | Short Text | 20 | Optional |

**Sample Data:**
```
request_id: #BR-2024-001
document_type: Barangay Clearance
full_name: Juan Dela Cruz
address: 123 Main St, Barangay San Isidro
contact_number: 09123456789
purpose: Employment
status: In Progress
date_requested: 2024-06-13
```

---

#### 3. **announcements** Table
Stores barangay announcements (for admin posting, resident viewing).

| Field Name | Data Type | Size | Properties |
|------------|-----------|------|------------|
| id | AutoNumber | - | Primary Key |
| announcement_id | Short Text | 50 | Required, Indexed (No Duplicates) |
| title | Short Text | 200 | Required |
| content | Long Text | - | Required |
| priority | Short Text | 20 | Required (values: "Emergency", "Urgent", "Normal", "Low") |
| category | Short Text | 50 | Required (values: "Health", "Safety & Security", "Environment", "Events", "Government Services", "Other") |
| posted_by | Short Text | 100 | Required |
| date_posted | Short Text | 20 | Required |

**Sample Data:**
```
announcement_id: ANN-0001
title: Community Clean-Up Drive
content: Join us this Saturday for our monthly community clean-up...
priority: Normal
category: Events
posted_by: Barangay Captain
date_posted: 2024-06-15 10:00:00
```

---

## How to Create the Database in Microsoft Access

### Step 1: Create New Database
1. Open Microsoft Access
2. Click "Blank Database"
3. Name it `barangay`
4. Save location: `C:\barangay.accdb`
5. Click "Create"

### Step 2: Create Tables

#### Creating the **users** table:
1. Click "Create" → "Table Design"
2. Add fields as shown in the table above
3. Set `id` as Primary Key (right-click → Primary Key)
4. For `email` field: In Field Properties, set "Indexed" to "Yes (No Duplicates)"
5. Save table as `users`

#### Creating the **document_requests** table:
1. Click "Create" → "Table Design"
2. Add all fields as shown in the table above
3. Set `id` as Primary Key
4. For `request_id` field: Set "Indexed" to "Yes (No Duplicates)"
5. Save table as `document_requests`

#### Creating the **announcements** table:
1. Click "Create" → "Table Design"
2. Add all fields as shown in the table above
3. Set `id` as Primary Key
4. For `announcement_id` field: Set "Indexed" to "Yes (No Duplicates)"
5. Save table as `announcements`

### Step 3: Add Sample Data
1. Open each table in Datasheet View
2. Manually enter the sample data provided above
3. Save and close

---

## Quick Setup Checklist

- [ ] Microsoft Access installed
- [ ] Created `barangay.accdb` at `C:\barangay.accdb`
- [ ] Created `users` table with all fields
- [ ] Created `document_requests` table with all fields
- [ ] Created `announcements` table with all fields
- [ ] Added at least 1 admin user (admin@barangay.com / admin123)
- [ ] Added at least 1 resident user (resident@email.com / resident123)
- [ ] Added sample document requests (optional)
- [ ] Added sample announcements (optional)
- [ ] Verified database connection in application

---

## Troubleshooting

### "Database not found" error
- Ensure `barangay.accdb` is located at `C:\barangay.accdb`
- Check file permissions (not read-only)

### "Object not found" error
- Verify all table names are exactly: `users`, `document_requests`, `announcements`
- Check that all required fields exist in each table
- Field names are case-sensitive

### Registration fails with "DATE_CREATED" error
- Make sure the `users` table has `created_at` field (not `date_created`)
- The field should be Short Text (20)

### Announcements not loading
- Verify `announcements` table exists
- Check that all required fields are present
- Add at least one sample announcement

---

## Default Login Credentials

**Admin Account:**
- Email: `admin@barangay.com`
- Password: `admin123`

**Resident Account:**
- Email: `resident@email.com`
- Password: `resident123`

---

## Notes
- All date fields use Short Text format (not Date/Time) for compatibility
- AutoNumber fields start at 1 and increment automatically
- Make sure to set proper indexes on unique fields
- Back up your database regularly
