# BARANGAY SAN ISIDRO RESIDENT PORTAL - COMPLETE FLOWCHART WITH INDICATIONS

## LEGEND

| Symbol       | Meaning                          |
|--------------|----------------------------------|
| (START/END)  | Oval — Process Start/End         |
| [PROCESS]    | Rectangle — Action/Operation     |
| {DECISION?}  | Diamond — Yes/No Branch          |
| >INPUT<      | Parallelogram — User Input       |
| ==SCREEN==   | Rounded Rectangle — UI Screen    |
| │ ▼ ► ◄     | Flow Direction Arrows            |

---

## 1. APPLICATION STARTUP FLOW

```
(START)
  │
  ▼
[App.java launches]
  │
  ▼
[initializeDatabase()]
  │
  ▼
{barangay.accdb exists?}
  ├── YES ──► [Connect to real barangay.accdb]
  └── NO  ──► [Create in-memory DB + sample data]
                    │
                    ▼
          {Connection successful?}
            ├── YES ──► [Initialize tables]
            │            [Load sample data]
            └── NO  ──► [Log warning]
                         [Demo mode ON]
                    │
                    ▼
            ==homepage.fxml==
                    │
                    ▼
          [Display Welcome Screen]
          [Show Login/Register]
```

> **INDICATION**: Database initialization happens BEFORE any UI loads. If DB fails, system runs in demo mode.

---

## 2. AUTHENTICATION FLOW

```
==homepage.fxml==
  │
  ├──────────────────────────────────────┐
  ▼                                      ▼
[Click Login]                      [Click Register]
  │                                      │
  ▼                                      ▼
==login.fxml==                     ==register.fxml==
  │                                      │
  ▼                                      ▼
>Enter email<                      >Enter email<
>Enter password<                   >Enter password<
  │                                >Confirm password<
  │                                >Enter first name<
  │                                >Enter last name<
  ▼                                      │
{Fields empty?}                          ▼
  ├── YES ──► [Show error]         {All fields valid?}
  └── NO  ──► [Query DB:             ├── NO  ──► [Show error]
               SELECT * FROM         └── YES ──► [Hash password]
               users WHERE                            │
               email=?]                               ▼
                    │                       [INSERT INTO users
                    ▼                        (email, password,
              {User found?}                  first_name, last_name,
                ├── NO ──► {Hardcoded        role='resident')]
                │           fallback?}               │
                │             ├── NO  ──► [Show error]  ▼
                │             └── YES ──► [Set User  {Insert success?}
                │                          Session]    ├── YES ──► [Show success]
                └── YES ──► {Password                  │            [Auto-redirect
                              matches?}                │             to login.fxml
                               ├── NO  ──► [Show error]│             after 2s]
                               └── YES ──► [Set User   └── NO  ──► [Show error]
                                            Session]
                    │
                    ▼
        [UserSession.setCurrentUser(email, role, name)]
                    │
                    ▼
          [loadDashboard("resident")]
                    │
                    ▼
          ==ResidentDashboard.fxml==
```

> **INDICATION**:
> - Login checks DB first, then hardcoded fallback
> - Register creates new user and auto-redirects to login
> - UserSession stores: email, role, full name

---

## 3. RESIDENT DASHBOARD FLOW

```
==ResidentDashboard.fxml==
  │
  ▼
[initialize() runs]
  │
  ├──────────────────┬──────────────────┐
  ▼                  ▼                  ▼
[loadUserProfile]  [loadDashboardStats] [syncNotifications]
  │                  │                  │
  └──────────────────┴──────────────────┘
                      │
                      ▼
              [Display name & email]
                      │
                      ▼
              [refreshAlertBadge()]
                      │
                      ▼
              {Unread count > 0?}
                ├── YES ──► [Show badge with count]
                └── NO  ──► [Hide badge]
                      │
                      ▼
  ┌───────────────────┼───────────────────┐
  ▼                   ▼                   ▼
[My Docs]       [Request Doc]      [Announcements]
[Complaints]    [Payments]         [Settings]
  │
  ▼
>User clicks module<
  │
  ▼
[Navigate to screen]
```

> **INDICATION**: Dashboard is the HUB — all modules accessible from here. Stats load from DB on initialize.

---

## 4. MY DOCUMENTS FLOW

```
==MyDocuments.fxml==
  │
  ▼
[initialize() runs]
  │
  ▼
[Query DB: SELECT * FROM document_requests WHERE user_email = ?]
  │
  ▼
{Has results?}
  ├── YES ──► [Build table with rows]
  └── NO  ──► [Show empty message]
                    │
                    ▼
          [Display columns:]
          - Request ID
          - Document Type
          - Request Date
          - Status (badge)
                    │
                    ▼
          {Status value?}
            ├── Pending     ──► 🟡
            ├── In Progress ──► 🔵
            ├── Ready       ──► 🟢
            └── Released    ──► ⚫
                    │
                    ▼
          >User clicks row<
                    │
                    ▼
          [showDocumentDetails()]
                    │
                    ▼
          [Open modal popup with:]
          - Full details
          - Timeline steps
          - Status history
                    │
                    ▼
          {Status = Ready?}
            ├── YES ──► [Show Download button]
            │                 │
            │                 ▼
            │           >Click Download<
            │                 │
            │                 ▼
            │           [Generate PDF/File]
            │                 │
            │                 ▼
            │           [Save to disk]
            └── NO  ──► [Show progress only]
```

> **INDICATION**:
> - Status badges color-coded: Pending=Yellow, In Progress=Blue, Ready=Green, Released=Gray
> - Only Ready/Released documents show download button

---

## 5. REQUEST DOCUMENT FLOW

```
==RequestDocument.fxml==
  │
  ▼
[initialize() runs]
  │
  ▼
[Load document types from DB]
  │
  ▼
>User selects document type<
  ├── [Barangay Clearance]
  ├── [Certificate of Residency]
  └── [Certificate of Indigency]
            │
            ▼
  [generateForm() — dynamic]
            │
            ▼
  [Show form fields based on type:]
  - Purpose
  - Additional details
  - File upload (optional)
            │
            ▼
  >User fills form<
            │
            ▼
  >User clicks Submit<
            │
            ▼
  [validateForm()]
            │
            ▼
  {All required fields filled?}
    ├── NO  ──► [Show error message]
    └── YES ──► [Generate request_id = REQ-YYYYMMDD-XXXX]
                      │
                      ▼
            [INSERT INTO document_requests
             (request_id, user_email, document_type,
              purpose, status='Pending', created_at)]
                      │
                      ▼
            {Insert success?}
              ├── NO  ──► [Show error]
              └── YES ──► [INSERT INTO notifications]
                                │
                                ▼
                          [Show success message]
                                │
                                ▼
                          [Timeline delay 3s]
                                │
                                ▼
                          [Navigate to MyDocuments.fxml]
```

> **INDICATION**:
> - Request ID auto-generated: REQ-20240115-0001
> - Notification created automatically on successful request
> - Auto-redirect after 3 seconds

---

## 6. ANNOUNCEMENTS FLOW

```
==ResidentAnnouncements.fxml==
  │
  ▼
[initialize() runs]
  │
  ▼
[Query DB: SELECT * FROM announcements ORDER BY id DESC]
  │
  ▼
{Has results?}
  ├── YES ──► [Build cards]
  └── NO  ──► [Load sample data]
                    │
                    ▼
          [Display each announcement as card:]
          - Title
          - Content
          - Date
          - Priority badge
                    │
                    ▼
          {Priority level?}
            ├── Emergency ──► 🔴
            ├── Urgent    ──► 🟠
            ├── Normal    ──► 🟢
            └── Low       ──► 🔵
                    │
                    ▼
          >User applies filter<
                    │
                    ▼
          {Filter type?}
            ├── [Priority]
            └── [Search text]
                    │
                    ▼
          [Reload filtered results]
                    │
                    ▼
          [Display filtered cards]
```

> **INDICATION**:
> - Priority badges: Emergency=Red, Urgent=Orange, Normal=Green, Low=Blue
> - Real-time filtering without page reload

---

## 7. COMPLAINTS FLOW

```
==Complaints_Resident.fxml==
  │
  ▼
[initialize() runs]
  │
  ▼
[Query DB: SELECT * FROM complaints WHERE user_email = ?]
  │
  ▼
{Has results?}
  ├── YES ──► [Display table]
  └── NO  ──► [Show empty message]
                    │
                    ▼
          [Show columns:]
          - Complaint ID
          - Type
          - Date Filed
          - Status
          - Actions
                    │
          ┌─────────┴──────────┐
          ▼                    ▼
  >Click "View Details"<  >Click "Submit New"<
          │                    │
          ▼                    ▼
  [openComplaintModal()]  [openSubmitModal()]
          │                    │
          ▼                    ▼
  [Show popup with:]      >Select incident type<
  - Incident type           ├── [Noise]
  - Location                ├── [Garbage]
  - Date occurred           └── [Others]
  - Description                  │
  - Status                       ▼
  - Admin response          >Enter location<
    (if any)                >Select date<
          │                 >Enter description<
          ▼                      │
  [Close modal]                  ▼
                           >Click Submit<
                                 │
                                 ▼
                           {All fields filled?}
                             ├── NO  ──► [Show error]
                             └── YES ──► [INSERT INTO complaints
                                          (user_email, type, location,
                                           date, description,
                                           status='Pending')]
                                               │
                                               ▼
                                         {Insert success?}
                                           ├── YES ──► [Show success]
                                           └── NO  ──► [Show error]
                                               │
                                               ▼
                                         [Close modal]
                                               │
                                               ▼
                                         [Reload complaints table]
```

> **INDICATION**:
> - All complaints start with status='Pending'
> - Admin response shows only if status changed to 'Resolved' or 'In Progress'

---

## 8. PAYMENTS FLOW

```
==ResidentPayments.fxml==
  │
  ▼
[initialize() runs]
  │
  ▼
[Query DB:
 SELECT p.*, dr.document_type
 FROM payments p
 LEFT JOIN document_requests dr
   ON p.document_request_id = dr.request_id
 WHERE p.user_email = ?]
  │
  ▼
{Has results?}
  ├── YES ──► [Display table]
  └── NO  ──► [Load sample data]
                    │
                    ▼
          [Show columns:]
          - Payment ID
          - Document Type
          - Amount
          - Status
          - Actions
                    │
                    ▼
          {Status = Pending?}
            ├── YES ──► [Show "Pay Now" button]
            └── NO  ──► [Show "✓ Paid" badge]
                              │
                              ▼
                        >User clicks "Pay Now"<
                              │
                              ▼
                        [handlePayment()]
                              │
                              ▼
                        [Show payment method modal]
                              │
                              ▼
                        >User selects method<
                          ├── [Online] ──► [PayMongo API call]
                          │                     │
                          │               {Link created?}
                          │                 ├── YES ──► [Show payment link]
                          │                 │           >Copy link<
                          │                 │           >Click "Simulate Payment"<
                          │                 └── NO  ──► [Show error]
                          │
                          └── [Cash] ──► [Show alert: "Pay at office"]
                                │
                                ▼
                        [UPDATE payments SET
                         status='Paid', payment_date=NOW()]
                                │
                                ▼
                        {Update success?}
                          ├── YES ──► [INSERT INTO notifications]
                          │                 │
                          │                 ▼
                          │           [Show success message]
                          │                 │
                          │                 ▼
                          │           [Reload payments table]
                          └── NO  ──► [Show error]
```

> **INDICATION**:
> - PayMongo integration for online payments
> - Cash option shows office address
> - Simulate button for testing (demo mode)
> - Notification sent on successful payment

---

## 9. NOTIFICATIONS FLOW (CROSS-CUTTING)

```
[ANY SCREEN initialize()]
  │
  ▼
[syncNotifications(userEmail)]
  │
  ▼
[Query DB: SELECT COUNT(*) FROM notifications
 WHERE user_email = ? AND is_read = 'false']
  │
  ▼
{Count > 0?}
  ├── YES ──► [Show badge with count]
  └── NO  ──► [Hide badge]
                    │
                    ▼
          >User clicks bell icon<
                    │
                    ▼
          [handleAlertsClick()]
                    │
                    ▼
          [Open notifications popup]
                    │
                    ▼
          [Show filter tabs:]
          - Unread
          - Past Notifications
                    │
                    ▼
          >User selects filter<
                    │
                    ▼
          [loadNotifications(showAll)]
                    │
                    ▼
          [Query DB based on filter]
                    │
                    ▼
          {Has notifications?}
            ├── YES ──► [Build list items]
            │           [Display each notification:]
            │           - Icon (📄 📢 📣)
            │           - Message
            │           - Timestamp
            │           - Read indicator (•)
            └── NO  ──► [Show "No notifications"]
                    │
                    ▼
          >User clicks notification<
                    │
                    ▼
          [markAsRead(notifId)]
                    │
                    ▼
          [UPDATE notifications SET is_read='true'
           WHERE notif_id = ?]
                    │
                    ▼
          [refreshAlertBadge()]
                    │
                    ▼
          [Update badge count]
```

> **INDICATION**:
> - Notifications sync on EVERY screen initialize
> - Badge shows count (max 99+)
> - Icons: 📄=Document, 📢=Complaint, 📣=Announcement
> - Blue dot (•) indicates unread

---

## 10. MY PROFILE FLOW

```
==MyProfile.fxml==
  │
  ▼
[initialize() runs]
  │
  ▼
[loadUserProfile()]
  │
  ▼
{user_email valid?}
  ├── NO  ──► [Show error] / [Load sample profile]
  └── YES ──► [Query DB:
               SELECT u.*, COUNT(dr.id)
               FROM users u
               LEFT JOIN document_requests dr
                 ON u.email = dr.user_email
               WHERE u.email = ?]
                    │
                    ▼
              {User found?}
                ├── NO  ──► [Load sample profile]
                └── YES ──► [Populate form fields:]
                             - First Name / Last Name
                             - Email / Phone / Address
                             - Gender / Date of Birth
                             - Member Since
                                  │
                    ┌─────────────┴──────────────┐
                    ▼                             ▼
            >Click "Edit"<               >Click "Change Password"<
                    │                             │
                    ▼                             ▼
            [toggleEditMode()]           [handlePasswordChange()]
            [Enable all fields]                   │
            [Show Save/Cancel]                    ▼
                    │                    >Enter current password<
                    ▼                    >Enter new password<
            >User modifies fields<       >Confirm new password<
                    │                             │
                    ▼                             ▼
            >Click "Save"<               {All fields filled?}
                    │                      ├── NO  ──► [Show error]
                    ▼                      └── YES ──► [Query DB: SELECT password
            [handleSave()]                              FROM users WHERE email=?]
                    │                                         │
                    ▼                                         ▼
            {user_email valid?}                    {Current password matches?}
              ├── NO  ──► [Show error]               ├── NO  ──► [Show error]
              └── YES ──► [validateForm()]            └── YES ──► {New passwords match?}
                                │                                    ├── NO  ──► [Show error]
                                ▼                                    └── YES ──► [UPDATE users
                          {All fields valid?}                                     SET password=?,
                            ├── NO  ──► [Show error]                              updated_at=?
                            └── YES ──► [UPDATE users                             WHERE email=?]
                                         SET first_name=?,                              │
                                         last_name=?, email=?,                          ▼
                                         contact_number=?,                    {Update success?}
                                         address=?, gender=?,                   ├── YES ──► [Show success]
                                         date_of_birth=?,                       │           [Clear fields]
                                         updated_at=?                           └── NO  ──► [Show error]
                                         WHERE email=?]
                                              │
                                              ▼
                                        {Update success?}
                                          ├── YES ──► [Update UserSession]
                                          │           [Show success]
                                          │           [toggleEditMode()]
                                          └── NO  ──► [Show error]
```

> **INDICATION**:
> - Profile uses JOIN to count total document requests
> - Email validation checks user_email exists in session
> - Password requires current password verification
> - All updates include updated_at timestamp

---

## 11. LOGOUT FLOW

```
[ANY SCREEN]
  │
  ▼
>User clicks Logout<
  │
  ▼
[handleLogout()]
  │
  ▼
[Clear UserSession]
  │
  ▼
[switchScene("login.fxml")]
  │
  ▼
==login.fxml==
  │
  ▼
(END)
```

> **INDICATION**: Logout clears session and returns to login screen.

---

## DATABASE FOREIGN KEY RELATIONSHIPS

```
┌──────────────────────────────────────────────────────────────┐
│                        users (Parent)                        │
│                        email (PK)                            │
└──────────────────────────────┬───────────────────────────────┘
                               │ user_email (FK)
           ┌───────────────────┼───────────────────┐
           ▼                                       ▼
┌──────────────────────┐               ┌───────────────────────┐
│  document_requests   │               │       payments        │
│  user_email (FK)     │◄──────────────│  user_email (FK)      │
│  request_id (PK)     │               │  document_request_id  │
└──────────────────────┘               └───────────────────────┘
           │
           ├──────────────┬──────────────┬──────────────┐
           ▼              ▼              ▼              ▼
┌──────────────┐ ┌──────────────┐ ┌──────────┐ ┌──────────────┐
│  complaints  │ │notifications │ │ settings │ │  audit_logs  │
│ user_email   │ │ user_email   │ │user_email│ │  user_email  │
│    (FK)      │ │    (FK)      │ │   (FK)   │ │    (FK)      │
└──────────────┘ └──────────────┘ └──────────┘ └──────────────┘
```

> **INDICATION**: All child tables reference users.email via user_email FK.

---

## VALIDATION CHECKS

### MyProfileController Validations

| Method                  | Checks                                                                 |
|-------------------------|------------------------------------------------------------------------|
| `loadUserProfile()`     | user_email != null, user_email not empty → else load sample profile    |
| `handleSave()`          | user_email valid, first/last name not empty, email format, phone 10-11 digits |
| `handlePasswordChange()`| user_email valid, current password not empty, new password ≥ 6 chars, passwords match |

---

## SYSTEM FLOW SUMMARY

```
(START)
  │
  ▼
[Database Init]
  │
  ▼
==Homepage== ──► ==Login / Register==
                        │
                        ▼
               ==Dashboard== (HUB)
                        │
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
  ==My Documents==  ==Request Doc==  ==Announcements==
  [View/Download]   [Submit]         [View/Filter]
                    [Notification]
        ▼               ▼               ▼
  ==Complaints==    ==Payments==    ==My Profile==
  [View/Submit]     [Pay Online/    [Edit/Save]
                     Cash]          [Change Password]
        │
        ▼
  [Logout] ──► ==Login== ──► (END)

[Notifications] = Cross-cutting concern (all screens)
```

---

## COLOR CODING GUIDE

| Color  | Meaning                              |
|--------|--------------------------------------|
| 🔴 Red    | Error, Emergency, Critical        |
| 🟠 Orange | Warning, Urgent                   |
| 🟡 Yellow | Pending, In Progress              |
| 🟢 Green  | Success, Ready, Normal            |
| 🔵 Blue   | Info, Low Priority                |
| ⚫ Gray   | Completed, Released, Inactive     |

---

## KEY TECHNICAL NOTES

| #  | Topic             | Detail                                                    |
|----|-------------------|-----------------------------------------------------------|
| 1  | MVC Pattern       | Model (DB + Session) → View (FXML) → Controller (Java)    |
| 2  | Database          | MS Access (barangay.accdb) via UCanAccess JDBC            |
| 3  | Session           | Static singleton UserSession (email, role, name)          |
| 4  | FK Relationships  | All tables link via user_email                            |
| 5  | Validation        | user_email checked before ALL DB operations               |
| 6  | Notifications     | Auto-sync on every screen initialize                      |
| 7  | Fallback          | Demo mode if DB unavailable                               |

---

**END OF FLOWCHART**
