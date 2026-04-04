# Database Field Names - Document Requests Table

## New Field Structure (Recommended & Implemented)

| Field Name          | Data Type    | Description                                    |
|---------------------|--------------|------------------------------------------------|
| id                  | AUTOINCREMENT| Primary key                                    |
| request_id          | TEXT(255)    | Unique request identifier (BC-2024-XXXX)       |
| document_type       | TEXT(255)    | Type of document requested                     |
| applicant_name      | TEXT(255)    | Full name of applicant (was: full_name)        |
| applicant_address   | MEMO         | Complete address (was: address)                |
| birth_date          | TEXT(50)     | Date of birth (was: birthdate)                 |
| civil_status        | TEXT(50)     | Marital status                                 |
| request_purpose     | TEXT(255)    | Purpose of request (was: purpose)              |
| residency_years     | TEXT(50)     | Years of residency (was: years_of_residency)   |
| req_status          | TEXT(50)     | Request status (was: status)                   |
| date_filed          | TEXT(50)     | Date request was filed (was: date_requested)   |
| resident_email      | TEXT(255)    | Email of resident (was: user_email)            |

## Changes Made

1. **Created UserSession.java** - Stores logged-in user information
2. **Updated DatabaseConnection.java** - New table structure with better field names
3. **Updated RequestDocumentController.java** - Uses new field names in INSERT query
4. **Updated MyDocumentsController.java** - Uses new field names in SELECT query
5. **Updated LoginController.java** - Sets UserSession on successful login

## Migration Strategy

The database will automatically detect old table structure and recreate the table with new field names on next connection.

## Benefits of New Field Names

- **Clearer naming**: "applicant_name" is more specific than "full_name"
- **Consistency**: All fields follow similar naming patterns
- **Professional**: Better aligned with database naming conventions
- **Shorter**: "req_status" and "date_filed" are more concise
- **Descriptive**: "residency_years" is clearer than "years_of_residency"
