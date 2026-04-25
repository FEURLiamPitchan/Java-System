# Database Column Names Fix Guide

## Problem
The Java code is using lowercase column names (e.g., `first_name`, `user_email`) but MS Access might have created them in a different case, causing SQL errors like:
- `user lacks privilege or object not found: FIRST_NAME`
- `user lacks privilege or object not found: USER_EMAIL`

## Solution
The database columns should match EXACTLY as defined in DatabaseConnection.java (lowercase with underscores).

## Required Column Names in barangay.accdb

### USERS Table
```
id (AUTOINCREMENT PRIMARY KEY)
email (TEXT 255)
password (TEXT 255)
role (TEXT 50)
first_name (TEXT 100)          ← Must be lowercase
last_name (TEXT 100)           ← Must be lowercase
contact_number (TEXT 20)       ← Must be lowercase with underscore
address (MEMO)
date_of_birth (TEXT 50)        ← Must be lowercase with underscores
gender (TEXT 20)
civil_status (TEXT 50)         ← Must be lowercase with underscore
occupation (TEXT 100)
profile_picture (TEXT 500)     ← Must be lowercase with underscore
is_active (YESNO)              ← Must be lowercase with underscore
created_at (TEXT 50)           ← Must be lowercase with underscore
```

### NOTIFICATIONS Table
```
notif_id (AUTOINCREMENT PRIMARY KEY)  ← Must be lowercase with underscore
user_email (TEXT 255)                  ← Must be lowercase with underscore
type (TEXT 50)
title (TEXT 255)
message (MEMO)
reference_id (TEXT 255)                ← Must be lowercase with underscore
reference_type (TEXT 50)               ← Must be lowercase with underscore
is_read (TEXT 10)                      ← Must be lowercase with underscore
priority (TEXT 20)
action_url (TEXT 500)                  ← Must be lowercase with underscore
created_at (TEXT 50)                   ← Must be lowercase with underscore
read_at (TEXT 50)                      ← Must be lowercase with underscore
```

### DOCUMENT_REQUESTS Table
```
id (AUTOINCREMENT PRIMARY KEY)
request_id (TEXT 255)                  ← Must be lowercase with underscore
user_email (TEXT 255)                  ← Must be lowercase with underscore
document_type (TEXT 255)               ← Must be lowercase with underscore
full_name (TEXT 255)                   ← Must be lowercase with underscore
age (INTEGER)
gender (TEXT 50)
birth_place (TEXT 255)                 ← Must be lowercase with underscore
birth_date (TEXT 50)                   ← Must be lowercase with underscore
civil_status (TEXT 50)                 ← Must be lowercase with underscore
address (MEMO)
years_residency (INTEGER)              ← Must be lowercase with underscore
contact_number (TEXT 50)               ← Must be lowercase with underscore
email_address (TEXT 255)               ← Must be lowercase with underscore
purpose (TEXT 255)
occupation (TEXT 255)
head_of_family (TEXT 255)              ← Must be lowercase with underscores
family_members (INTEGER)               ← Must be lowercase with underscore
monthly_income (CURRENCY)              ← Must be lowercase with underscore
income_source (TEXT 255)               ← Must be lowercase with underscore
valid_id_path (TEXT 500)               ← Must be lowercase with underscores
proof_of_residency_path (TEXT 500)     ← Must be lowercase with underscores
proof_of_income_path (TEXT 500)        ← Must be lowercase with underscores
status (TEXT 50)
date_requested (TEXT 50)               ← Must be lowercase with underscore
date_completed (TEXT 50)               ← Must be lowercase with underscore
created_at (TEXT 50)                   ← Must be lowercase with underscore
```

## How to Fix Your Database

### Option 1: Delete and Recreate (RECOMMENDED - Will lose data)
1. Close your Java application
2. Delete `C:\Projects 2\dashboard_resident\barangay.accdb`
3. Run your Java application again
4. DatabaseConnection.java will automatically create a new database with correct column names

### Option 2: Manually Rename Columns in MS Access (Preserves data)
1. Open `barangay.accdb` in Microsoft Access
2. For each table (users, notifications, document_requests):
   - Open the table in Design View
   - Check each column name
   - If any column has UPPERCASE or different naming (e.g., `FirstName` instead of `first_name`), rename it to match the list above
   - Save the table
3. Close MS Access
4. Run your Java application

### Option 3: Run Database Recreation Script
The DatabaseConnection.java will check if tables exist. If you want to force recreation:
1. Backup your current database (copy barangay.accdb to barangay_backup.accdb)
2. Delete barangay.accdb
3. Run the application - it will create fresh tables with correct names

## Verification
After fixing, all these SQL queries should work:
```sql
SELECT first_name, last_name FROM users WHERE email = ?
SELECT COUNT(*) FROM notifications WHERE user_email = ? AND is_read = 'false'
UPDATE users SET first_name = ?, last_name = ? WHERE email = ?
```

## Important Notes
- MS Access column names are CASE-SENSITIVE when accessed through JDBC
- Always use lowercase with underscores (snake_case) as defined in DatabaseConnection.java
- The Java code expects exact matches - `first_name` ≠ `FirstName` ≠ `FIRST_NAME`
