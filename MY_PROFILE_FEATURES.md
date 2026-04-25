# MY PROFILE - NEW FEATURES DOCUMENTATION

## Overview
Enhanced My Profile page for Barangay San Isidro Resident Portal with complete profile management features adapted from the Admin system.

---

## ✨ NEW FEATURES ADDED

### 1. Profile Picture Management

#### **Upload Profile Picture**
- **Button**: "Change Picture" (Blue button)
- **Functionality**:
  - Opens file chooser dialog
  - Accepts: PNG, JPG, JPEG, GIF
  - Max file size: 2MB
  - Validates file size before upload
  - Saves to `profile_pictures/` directory
  - Filename format: `{email}_{timestamp}.{ext}`
  - Updates database `users.profile_picture` field
  - Updates `updated_at` timestamp

#### **Remove Profile Picture**
- **Button**: "Remove" (Gray button)
- **Functionality**:
  - Shows confirmation dialog
  - Removes picture from database (sets to NULL)
  - Deletes physical file from disk
  - Reverts to initials display
  - Updates `updated_at` timestamp

---

### 2. Profile Avatar Display

#### **With Profile Picture**
- Displays uploaded photo in circular crop
- 100px diameter circle (radius 50)
- 2px border (#e0e0e0)
- Hides initials label when picture exists

#### **Without Profile Picture (Initials Fallback)**
- Shows first letter of first name + first letter of last name
- Example: "Juan Dela Cruz" → "JD"
- Displayed in white text on dark background (#2d2d2d)
- Font size: 32px, bold
- Centered in circular avatar
- Default: "R" if no name available

---

### 3. Status Badge

#### **Active Status**
- **Display**: Green badge
- **Background**: #e8f5e9
- **Text Color**: #4caf50
- **Text**: "Active"

#### **Inactive Status**
- **Display**: Red badge
- **Background**: #ffebee
- **Text Color**: #f44336
- **Text**: "Inactive"

**Database Field**: `users.is_active` (true/false or 1/0)

---

### 4. Role Badge

#### **Display**
- **Background**: #e3f2fd (Light Blue)
- **Text Color**: #2196f3 (Blue)
- **Text**: Capitalized role name (e.g., "Resident", "Admin")
- **Font**: 11px, bold
- **Padding**: 4px 10px
- **Border Radius**: 4px

**Database Field**: `users.role`

---

### 5. Email Cascade Updates

When user changes their email address, the system automatically updates all related tables:

#### **Tables Updated**:
1. **users** - Primary email field
2. **document_requests** - `user_email` FK
3. **payments** - `user_email` FK
4. **complaints** - `user_email` FK
5. **notifications** - `user_email` FK
6. **settings** - `user_email` FK

#### **Process**:
```
1. User changes email in profile
2. Click "Save Changes"
3. System validates new email
4. Updates users table
5. Cascades to all child tables (FK relationships)
6. Updates UserSession with new email
7. Shows success message
8. All future queries use new email
```

**IMPORTANT**: Email cascade ensures data integrity across all modules. User's document requests, payments, complaints, and notifications remain linked after email change.

---

## 🎨 UI LAYOUT

### Left Panel - Profile Card (300px width)
```
┌─────────────────────────────────┐
│  Profile Picture                │
│                                 │
│      ┌─────────────┐            │
│      │   Avatar    │            │
│      │  (Circle)   │            │
│      │   or "JD"   │            │
│      └─────────────┘            │
│                                 │
│    Juan Dela Cruz               │
│    juan@email.com               │
│                                 │
│  [Change Picture]  [Remove]     │
│                                 │
├─────────────────────────────────┤
│  Role                           │
│  [Resident]                     │
├─────────────────────────────────┤
│  Account Status                 │
│  [Active]                       │
├─────────────────────────────────┤
│  Member Since                   │
│  January 2024                   │
└─────────────────────────────────┘
```

### Right Panel - Account Info & Forms
```
┌─────────────────────────────────────────┐
│  Personal Information        [Edit]     │
│                                         │
│  First Name *        Last Name *        │
│  [Juan          ]    [Dela Cruz    ]    │
│                                         │
│  Email Address *     Phone Number *     │
│  [juan@email.com]    [09123456789  ]    │
│                                         │
│  Date of Birth       Gender             │
│  [01/01/1990    ]    [Male ▼       ]    │
│                                         │
│  Complete Address *                     │
│  [123 Main St, Brgy San Isidro...  ]    │
│                                         │
│  [Save Changes]  [Cancel]               │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  Change Password                        │
│                                         │
│  Current Password                       │
│  [••••••••••••]                         │
│                                         │
│  New Password                           │
│  [••••••••••••]                         │
│                                         │
│  Confirm New Password                   │
│  [••••••••••••]                         │
│                                         │
│  [Update Password]                      │
└─────────────────────────────────────────┘
```

---

## 🔧 TECHNICAL IMPLEMENTATION

### Database Schema
```sql
-- users table (already exists)
CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT DEFAULT 'resident',
    first_name TEXT,
    last_name TEXT,
    contact_number TEXT,
    address TEXT,
    date_of_birth TEXT,
    gender TEXT,
    profile_picture TEXT,        -- NEW: Path to profile picture
    is_active TEXT DEFAULT 'true', -- NEW: Account status
    created_at TEXT,
    updated_at TEXT
);
```

### File Storage
```
project_root/
├── profile_pictures/           -- NEW: Profile pictures directory
│   ├── user_email_com_1234567890.jpg
│   ├── another_user_1234567891.png
│   └── ...
└── dashboard_resident/
    └── ...
```

### Controller Methods

#### New Methods Added:
1. **setupProfileAvatar()** - Initializes avatar circle with border
2. **updateStatusBadge(String isActive)** - Updates status badge color/text
3. **updateRoleBadge(String role)** - Updates role badge text
4. **loadProfilePicture(String path, String firstName, String lastName)** - Loads picture or shows initials
5. **showInitials(String firstName, String lastName)** - Displays initials in avatar
6. **handleChangePicture()** - Opens file chooser and uploads picture
7. **handleRemovePicture()** - Removes picture with confirmation
8. **cascadeEmailUpdate(Connection conn, String oldEmail, String newEmail)** - Updates email across all tables

#### Modified Methods:
1. **loadUserProfile()** - Now loads role, status, and profile picture
2. **handleSave()** - Now cascades email changes to related tables

---

## 📋 VALIDATION RULES

### Profile Picture Upload
- ✅ File type: PNG, JPG, JPEG, GIF only
- ✅ Max size: 2MB
- ✅ File must exist and be readable
- ❌ Shows error if file too large
- ❌ Shows error if upload fails

### Email Change
- ✅ Must be valid email format (contains @)
- ✅ Cannot be empty
- ✅ Cascades to all related tables
- ✅ Updates UserSession immediately
- ✅ Updates all FK relationships

### Profile Fields
- ✅ First Name: Required
- ✅ Last Name: Required
- ✅ Email: Required, valid format
- ✅ Phone: Required, 10-11 digits
- ✅ Address: Required
- ⚠️ Date of Birth: Optional
- ⚠️ Gender: Optional (default: "Prefer not to say")

---

## 🔄 USER WORKFLOW

### Changing Profile Picture
```
1. User clicks "Change Picture"
2. File chooser opens
3. User selects image file
4. System validates file size (< 2MB)
5. System copies file to profile_pictures/
6. System updates database
7. Avatar displays new picture
8. Success message shown
```

### Removing Profile Picture
```
1. User clicks "Remove"
2. Confirmation dialog appears
3. User clicks "OK"
4. System removes picture from database
5. System deletes physical file
6. Avatar shows initials
7. Success message shown
```

### Editing Profile with Email Change
```
1. User clicks "Edit"
2. Fields become editable
3. User changes email (e.g., old@email.com → new@email.com)
4. User clicks "Save Changes"
5. System validates all fields
6. System updates users table
7. System cascades email to:
   - document_requests
   - payments
   - complaints
   - notifications
   - settings
8. System updates UserSession
9. Success message shown
10. Fields become read-only
11. Top bar shows new email immediately
```

---

## 🎯 KEY FEATURES SUMMARY

| Feature | Status | Description |
|---------|--------|-------------|
| Profile Picture Upload | ✅ | Upload PNG/JPG/JPEG/GIF, max 2MB |
| Profile Picture Remove | ✅ | Remove with confirmation dialog |
| Avatar with Initials | ✅ | Shows initials when no picture |
| Status Badge | ✅ | Active (green) / Inactive (red) |
| Role Badge | ✅ | Displays user role (Resident) |
| Email Cascade | ✅ | Updates email across 6 tables |
| Member Since | ✅ | Formatted date display |
| Edit Mode | ✅ | Toggle editable fields |
| Password Change | ✅ | Separate password update section |
| Validation | ✅ | All fields validated before save |

---

## 🔗 FOREIGN KEY RELATIONSHIPS

```
users (email)
    ↓ user_email (FK)
    ├── document_requests
    ├── payments
    ├── complaints
    ├── notifications
    └── settings

When email changes:
old@email.com → new@email.com
    ↓
All child tables updated automatically
```

---

## 🚀 TESTING CHECKLIST

### Profile Picture
- [ ] Upload valid image (PNG, JPG, JPEG, GIF)
- [ ] Upload image > 2MB (should show error)
- [ ] Upload non-image file (should be blocked by file chooser)
- [ ] Remove picture (should show confirmation)
- [ ] Remove picture when no picture exists (should handle gracefully)
- [ ] Initials display correctly (first + last name)
- [ ] Initials show "R" when no name available

### Status & Role Badges
- [ ] Active status shows green badge
- [ ] Inactive status shows red badge
- [ ] Role badge shows "Resident"
- [ ] Badges load from database correctly

### Email Cascade
- [ ] Change email and save
- [ ] Check document_requests table (user_email updated)
- [ ] Check payments table (user_email updated)
- [ ] Check complaints table (user_email updated)
- [ ] Check notifications table (user_email updated)
- [ ] Check settings table (user_email updated)
- [ ] UserSession updated with new email
- [ ] Top bar shows new email immediately
- [ ] Can still access all previous data with new email

### Edit Mode
- [ ] Click "Edit" - fields become editable
- [ ] Click "Cancel" - fields revert to original values
- [ ] Save with valid data - success message shown
- [ ] Save with invalid data - error message shown
- [ ] Fields become read-only after save

### Password Change
- [ ] Change password with correct current password
- [ ] Try to change with wrong current password (should fail)
- [ ] Try to change with mismatched new passwords (should fail)
- [ ] Try to change with password < 6 characters (should fail)
- [ ] Successful password change clears all password fields

---

## 📝 NOTES

1. **Profile Pictures Directory**: Created automatically on first upload
2. **File Naming**: Uses email + timestamp to avoid conflicts
3. **Email Cascade**: Ensures data integrity across all modules
4. **UserSession**: Updated immediately after email change
5. **Initials Fallback**: Always available even if picture upload fails
6. **Status Badge**: Reads from `is_active` field (true/false or 1/0)
7. **Role Badge**: Reads from `role` field, capitalizes first letter
8. **Member Since**: Formatted as "Month Year" (e.g., "January 2024")

---

## 🔐 SECURITY CONSIDERATIONS

1. **File Upload**:
   - File type validation (image files only)
   - File size validation (max 2MB)
   - Unique filename generation (prevents overwrite)
   - Stored outside web root (if applicable)

2. **Email Change**:
   - Validates email format
   - Cascades to all related tables (maintains FK integrity)
   - Updates session immediately
   - Prevents orphaned records

3. **Password Change**:
   - Requires current password verification
   - Minimum 6 characters
   - Confirmation required
   - Updates timestamp

---

**END OF DOCUMENTATION**
