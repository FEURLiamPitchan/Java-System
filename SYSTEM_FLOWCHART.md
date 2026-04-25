# Barangay San Isidro - Resident Portal System Flowchart

## Architecture: MVC (Model-View-Controller)

| Layer | Files |
|-------|-------|
| **Model** | `DatabaseConnection`, `UserSession`, `ResidentNotifications`, `DocumentRequest`, `PaymentRecord` |
| **View** | All `.fxml` files (homepage, login, register, ResidentDashboard, etc.) |
| **Controller** | All `*Controller.java` files |

---

## Symbol Legend

| Symbol | Shape | Meaning |
|--------|-------|---------|
| `(START / END)` | Oval / Terminator | Start or end of a process |
| `[PROCESS]` | Rectangle | An action or operation |
| `{DECISION}` | Diamond | A yes/no or branching condition |
| `>INPUT<` | Parallelogram | User input |
| `==SCREEN==` | Rounded Rectangle | A UI screen (FXML view) |

---

## 1. Application Startup & Database Initialization

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
   YES │        │ NO
       ▼        ▼
[Connect to   [Create in-memory
 real .accdb]  DB + sample data]
       │        │
       └───┬────┘
           ▼
{Connection successful?}
   YES │        │ NO
       ▼        ▼
[Init tables] [Log warning,
[Load data]    demo mode]
       │        │
       └───┬────┘
           ▼
    ==homepage.fxml==
```

---

## 2. Authentication Flow

```
==homepage.fxml==
       │
   ┌───┴───────────────┐
   ▼                   ▼
==login.fxml==    ==register.fxml==
   │                   │
   ▼                   ▼
>Enter email        >Enter email,
 + password<         password,
                      confirm<
   │                   │
   ▼                   ▼
{Fields empty?}    {Fields valid?}
 YES │   │ NO       NO │   │ YES
     ▼   │             ▼   │
[showError]        [showError]  │
         │                     ▼
         ▼             [INSERT INTO users]
[authenticateUser()]           │
         │                {SQL success?}
         ▼              NO │   │ YES
{User found in DB?}         ▼   ▼
  NO │    │ YES      [showError] [showMessage
     ▼    │                  "Account created"]
{Hardcoded
 fallback match?}
  NO │    │ YES
     ▼    ▼
[showError] [UserSession.setCurrentUser()]
                    │
                    ▼
        [loadDashboard("resident")]
```

---

## 3. Resident Dashboard

```
==ResidentDashboard.fxml==
[initialize():
 loadUserProfile()
 loadDashboardStats()
 syncNotifications()
 refreshAlertBadge()]
         │
   ┌─────┼──────────────────────────┐
   │     │         │        │       │
   ▼     ▼         ▼        ▼       ▼
[My   [Request  [Announce [Comp-  [Pay-
 Docs] Document] ments]   laints] ments]
```

---

## 4. My Documents

```
==MyDocuments.fxml==
   │
   ▼
[Query DB: document_requests WHERE user_email]
   │
   ▼
{Has data?}
  NO │   │ YES
     ▼   ▼
[Show  [Display rows with
 empty  status badges:
 label]  Pending / In Progress
         / Ready / Released]
              │
              ▼
         >Click row<
              │
              ▼
         [showDocumentDetails()]
              │
              ▼
         {Status = Ready/Released?}
           YES │    │ NO
               ▼    ▼
         [Show    [Show progress
          Download  steps only]
          button]
```

---

## 5. Request Document

```
==RequestDocument.fxml==
   │
   ▼
>Select document type<
   │
   ├── Barangay Clearance
   ├── Certificate of Residency
   └── Certificate of Indigency
   │
   ▼
[generateForm() - dynamic fields]
   │
   ▼
>Fill form + upload files<
   │
   ▼
[validateForm()]
   │
   ▼
{Valid?}
  NO │   │ YES
     ▼   │
[showStatus    │
 error]        ▼
          [INSERT INTO document_requests]
               │
               ▼
          [createNotification()]
               │
               ▼
          [showStatus success]
               │
               ▼
          [Timeline 3s delay]
               │
               ▼
          [switchScene → MyDocuments.fxml]
```

---

## 6. Announcements

```
==ResidentAnnouncements.fxml==
   │
   ▼
[Query DB: announcements ORDER BY id DESC]
   │
   ▼
{DB has data?}
  NO │   │ YES
     ▼   ▼
[Sample  [Build cards with
 data]    priority badges:
          Emergency / Urgent
          / Normal / Low]
              │
              ▼
         {Filter / Search applied?}
           YES │    │ NO
               ▼    ▼
         [Reload   [Show all
          filtered  cards]
          results]
```

---

## 7. Complaints

```
==Complaints_Resident.fxml==
   │
   ▼
[Query DB: complaints WHERE user_email]
   │
   ▼
{DB has data?}
  NO │   │ YES
     ▼   ▼
[Show   [Display rows]
 empty]      │
             ▼
        >Click "View Details"<
             │
             ▼
        [openComplaintModal()]
             │
             ▼
        [Show: type, location,
         date, status, details,
         admin response (if any)]
             │
        ─────────────────────
        >Click "Submit New"<
             │
             ▼
        >Select incident type
         + date<
             │
             ▼
        {Fields filled?}
          NO │   │ YES
             ▼   ▼
        [showError] [INSERT INTO complaints
                     status = 'Pending']
                          │
                          ▼
                    [Reload complaints]
```

---

## 8. Payments

```
==ResidentPayments.fxml==
   │
   ▼
[Query DB: payments WHERE user_email]
   │
   ▼
{DB has data?}
  NO │   │ YES
     ▼   ▼
[Sample  [Display rows]
 data]        │
              ▼
         {Status = Pending?}
          YES │    │ NO
              ▼    ▼
         [Pay Now] [✓ Completed]
              │
              ▼
         [handlePayment()]
              │
         {Choose method?}
        ONLINE │    │ CASH
               ▼    ▼
      [PayMongo    [Show office
       createLink]  info alert]
               │
          {Link created?}
          YES │    │ NO
              ▼    ▼
       [Show URL  [Alert error]
        + copy btn]
              │
              ▼
       >Click "Simulate Payment"<
              │
              ▼
       [UPDATE payments SET status = Paid]
              │
              ▼
       [addNotification()]
              │
              ▼
       [Reload payments list]
```

---

## 9. Notifications (Cross-Cutting — All Screens)

```
Every screen initialize():
   │
   ▼
[syncNotifications(userEmail)]
   │
   ▼
{Unread count > 0?}
 YES │    │ NO
     ▼    ▼
[Show   [Hide
 badge]  badge]
     │
     ▼
>Click bell icon<
     │
     ▼
==Notifications Popup==
     │
     ▼
{Filter: Unread or All?}
     │
     ▼
[Query notifications
 WHERE user_email
 (+ is_read filter)]
     │
     ▼
>Click notification item<
     │
     ▼
[showNotifDetail()]
     │
     ▼
{Mark as read?}
 YES │
     ▼
[UPDATE notifications SET is_read = true]
     │
     ▼
[refreshAlertBadge()]
```

---

## 10. Logout (Any Screen)

```
>Click Logout button<
   │
   ▼
[switchScene("login.fxml")]
   │
   ▼
(END)
```

---

## Database Tables Reference

| Table | Purpose |
|-------|---------|
| `users` | Login credentials and profile info |
| `document_requests` | All document requests per resident |
| `document_types` | Available document types and fees |
| `payments` | Payment records linked to requests |
| `complaints` | Resident-filed complaints |
| `announcements` | Barangay announcements |
| `notifications` | Per-user notification inbox |
| `settings` | User preferences |
| `system_settings` | Barangay-wide config |
| `audit_logs` | Activity tracking |
| `barangay_officials` | Officials directory |

---

## Architecture Notes

- Every `==SCREEN==` maps to one `.fxml` (View) + one `Controller.java` (Controller)
- Every `[DB operation]` goes through `DatabaseConnection.getConnection()` (Model)
- `UserSession` is a static singleton shared across all controllers — no dependency injection
- `ResidentNotifications` acts as a service layer between controllers and the `notifications` table
- All `{Decision}` branches that hit "NO DB" fall back gracefully to sample/demo data
- Scene transitions use `FadeTransition` (200ms) in some controllers for smooth navigation
