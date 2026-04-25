# ACTUAL DATABASE STRUCTURE - barangay.accdb

## CRITICAL: Use these EXACT column names in all Java code

---

## TABLE: users
| Column Name          | Data Type | Notes                                    |
|----------------------|-----------|------------------------------------------|
| id                   | Int32     | Primary Key (AUTOINCREMENT)              |
| email                | String    | User email (login)                       |
| password             | String    | Hashed password                          |
| role                 | String    | 'admin' or 'resident'                    |
| full_name            | String    | Full name of user                        |
| status               | String    | Account status                           |
| date_created         | DateTime  | Account creation date                    |
| profile_picture      | String    | Path to profile picture                  |
| verified_by          | Int32     | ID of admin who verified                 |
| verification_status  | String    | Verification status                      |

---

## TABLE: residents
| Column Name     | Data Type | Notes                                    |
|-----------------|-----------|------------------------------------------|
| id              | Int32     | Primary Key (AUTOINCREMENT)              |
| full_name       | String    | Full name                                |
| age             | Int32     | Age                                      |
| address         | String    | Complete address                         |
| status          | String    | Resident status                          |
| date_added      | String    | Date added to system                     |
| gender          | String    | Male/Female/Other                        |
| birth_place     | String    | Place of birth                           |
| birth_date      | DateTime  | Date of birth                            |
| civil_status    | String    | Single/Married/Divorced/Widowed          |
| contact_number  | String    | Contact number                           |
| user_id         | Int16     | Foreign key to users table               |

---

## TABLE: document_requests
| Column Name              | Data Type | Notes                                    |
|--------------------------|-----------|------------------------------------------|
| id                       | Int32     | Primary Key (AUTOINCREMENT)              |
| request_id               | String    | Unique request ID (REQ-YYYYMMDD-XXXX)    |
| document_type            | String    | Type of document requested               |
| years_residency          | Double    | Years living in barangay                 |
| email_address            | String    | Email of requester                       |
| purpose                  | String    | Purpose of request                       |
| occupation               | String    | Occupation                               |
| head_of_family           | String    | Head of family name                      |
| family_members           | Double    | Number of family members                 |
| monthly_income           | Decimal   | Monthly income                           |
| income_source            | String    | Source of income                         |
| valid_id_path            | String    | Path to valid ID file                    |
| proof_of_residency_path  | String    | Path to proof of residency file          |
| proof_of_income_path     | String    | Path to proof of income file             |
| status                   | String    | Pending/In Progress/Ready/Released       |
| date_requested           | String    | Date request was made                    |
| date_completed           | String    | Date request was completed               |
| created_at               | String    | Timestamp of creation                    |
| barangay_province        | String    | Province                                 |
| barangay_city            | String    | City                                     |
| or_number                | String    | Official receipt number                  |
| resident_id              | Double    | Foreign key to residents table           |
| requested_by             | Double    | ID of user who requested                 |

**MISSING COLUMNS** (that code expects but don't exist):
- full_name
- age
- gender
- birth_place
- birth_date
- civil_status
- address
- contact_number
- user_email

**SOLUTION**: These fields should be retrieved from the `residents` table using JOIN with `resident_id`

---

## TABLE: payments
| Column Name    | Data Type | Notes                                    |
|----------------|-----------|------------------------------------------|
| id             | Int32     | Primary Key (AUTOINCREMENT)              |
| payment_id     | String    | Unique payment ID                        |
| ref_number     | String    | Reference number                         |
| payment_type   | String    | Type of payment                          |
| amount         | Int32     | Payment amount                           |
| status         | String    | Pending/Paid/Failed                      |
| date_created   | String    | Date payment was created                 |
| archived       | Boolean   | Whether payment is archived              |
| resident_id    | Double    | Foreign key to residents table           |
| request_id     | String    | Foreign key to document_requests         |

**MISSING COLUMNS** (that code expects):
- user_email
- document_request_id
- description
- payment_method
- transaction_id
- paymongo_link_id
- payment_date
- due_date
- receipt_number
- receipt_path
- notes
- processed_by
- updated_at

---

## TABLE: complaints
| Column Name        | Data Type | Notes                                    |
|--------------------|-----------|------------------------------------------|
| ID                 | Int32     | Primary Key (AUTOINCREMENT) - NOTE: UPPERCASE |
| complaint_id       | String    | Unique complaint ID                      |
| complainant_name   | String    | Name of complainant                      |
| incident_type      | String    | Type of incident                         |
| incident_details   | String    | Details of incident                      |
| location           | String    | Location of incident                     |
| incident_date      | String    | Date of incident                         |
| photo_path         | String    | Path to photo evidence                   |
| status             | String    | Open/In Progress/Resolved/Closed         |
| admin_response     | String    | Admin's response                         |
| date_filed         | String    | Date complaint was filed                 |
| is_read            | Boolean   | Whether admin has read                   |
| status_changed_at  | DateTime  | When status was last changed             |
| resident_id        | String    | Foreign key to residents table           |
| updated_at         | DateTime  | Last update timestamp                    |

**MISSING COLUMNS** (that code expects):
- user_email
- complainant_contact
- complainant_address
- incident_category
- priority
- evidence_photos
- witness_info
- resolution
- assigned_to
- resolved_date
- is_anonymous
- created_at

---

## TABLE: announcements
| Column Name      | Data Type | Notes                                    |
|------------------|-----------|------------------------------------------|
| id               | Int32     | Primary Key (AUTOINCREMENT)              |
| announcement_id  | String    | Unique announcement ID                   |
| title            | String    | Announcement title                       |
| content          | String    | Announcement content                     |
| priority         | String    | Low/Medium/High/Critical                 |
| category         | String    | Category of announcement                 |
| posted_by        | String    | Who posted the announcement              |
| date_posted      | DateTime  | Date posted                              |
| preview_length   | Double    | Length of preview                        |
| created_at       | DateTime  | Creation timestamp                       |
| updated_at       | DateTime  | Last update timestamp                    |

**MISSING COLUMNS** (that code expects):
- summary
- target_audience
- expiry_date
- is_active
- is_featured
- image_path
- attachment_path
- view_count

---

## TABLE: notifications
| Column Name  | Data Type | Notes                                    |
|--------------|-----------|------------------------------------------|
| notif_id     | Int32     | Primary Key (AUTOINCREMENT)              |
| type         | String    | document/complaint/announcement          |
| message      | String    | Notification message                     |
| reference_id | String    | ID of related record                     |
| is_read      | Boolean   | Whether notification has been read       |
| created_at   | DateTime  | Creation timestamp                       |
| user_id      | Double    | Foreign key to users/residents table     |

**MISSING COLUMNS** (that code expects):
- user_email
- title
- reference_type
- priority
- action_url
- read_at

**CRITICAL**: Uses `user_id` (numeric) instead of `user_email` (string)

---

## TABLE: settings
| Column Name           | Data Type | Notes                                    |
|-----------------------|-----------|------------------------------------------|
| setting_id            | Int32     | Primary Key (AUTOINCREMENT)              |
| dark_mode             | String    | 'true' or 'false'                        |
| font_size             | String    | Font size setting                        |
| notif_complaints      | String    | Enable complaint notifications           |
| notif_payments        | String    | Enable payment notifications             |
| notif_announcements   | String    | Enable announcement notifications        |
| base_population       | Int32     | Base population count                    |
| user_id               | Double    | Foreign key to users table               |

**MISSING COLUMNS** (that code expects):
- id
- user_email
- notif_documents
- created_at
- updated_at

**CRITICAL**: Uses `user_id` (numeric) instead of `user_email` (string)

---

## TABLE: finances
| Column Name   | Data Type | Notes                                    |
|---------------|-----------|------------------------------------------|
| finance_id    | Int32     | Primary Key (AUTOINCREMENT)              |
| category      | String    | Finance category                         |
| description   | String    | Description                              |
| type          | String    | Income/Expense                           |
| amount        | Decimal   | Amount                                   |
| status        | String    | Status                                   |
| date_recorded | DateTime  | Date recorded                            |
| recorded_by   | String    | Who recorded it                          |
| created_at    | DateTime  | Creation timestamp                       |
| updated_at    | DateTime  | Last update timestamp                    |

---

## TABLE: logs
| Column Name  | Data Type | Notes                                    |
|--------------|-----------|------------------------------------------|
| log_id       | Int32     | Primary Key (AUTOINCREMENT)              |
| action       | String    | Action performed                         |
| performed_by | String    | Who performed the action                 |
| log_date     | DateTime  | Date of log entry                        |

---

## CRITICAL ISSUES TO FIX

### 1. **document_requests table**
- **Problem**: Missing personal information columns (full_name, age, gender, etc.)
- **Solution**: These fields exist in `residents` table. Need to:
  1. Get `resident_id` from `residents` table using user's email
  2. Store only `resident_id` in document_requests
  3. Use JOIN queries to retrieve full information

### 2. **notifications table**
- **Problem**: Uses `user_id` (numeric) instead of `user_email` (string)
- **Solution**: Need to:
  1. Get `user_id` from `users` table using email
  2. Store `user_id` in notifications
  3. Convert all notification queries to use user_id

### 3. **settings table**
- **Problem**: Uses `user_id` (numeric) instead of `user_email` (string)
- **Solution**: Same as notifications - use user_id instead of user_email

### 4. **UserSession class**
- **Problem**: Only stores email, but database needs user_id
- **Solution**: Add `userId` field to UserSession and populate it on login

---

## RECOMMENDED APPROACH

### Option 1: Modify Database (NOT RECOMMENDED - user said database is updated)
Add missing columns to match code expectations

### Option 2: Modify Java Code (RECOMMENDED)
Update all Java controllers to:
1. Use exact column names from database
2. Implement JOIN queries where needed
3. Use user_id instead of user_email for foreign keys
4. Update UserSession to store user_id
5. Create helper methods to convert between user_email and user_id

---

## FILES THAT NEED UPDATES

1. **DatabaseConnection.java** - Remove table creation code that doesn't match actual structure
2. **UserSession.java** - Add userId field
3. **LoginController.java** - Store userId in session
4. **RegisterController.java** - Create both users and residents records
5. **MyDocumentsController.java** - Use JOIN with residents table
6. **RequestDocumentController.java** - Insert into residents table first, then document_requests
7. **ResidentPaymentsController.java** - Use correct column names
8. **Complaints_ResidentController.java** - Use correct column names
9. **ResidentAnnouncementsController.java** - Use correct column names
10. **ResidentNotifications.java** - Use user_id instead of user_email
11. **ResidentSettingsController.java** - Use user_id instead of user_email
12. **MyProfileController.java** - Update to work with users + residents tables
13. **ResidentDashboardController.java** - Update queries to match actual structure

---

## NEXT STEPS

1. Backup current code
2. Update UserSession.java to include userId
3. Update LoginController.java to populate userId
4. Update all controllers one by one to use correct column names
5. Test each module after update
6. Update DatabaseConnection.java to remove incorrect table creation code

