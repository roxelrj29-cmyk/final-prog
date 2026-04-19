# Project 5: Event Registration and Attendee Management System
**Gordon College – College of Computer Studies**

## Group Members
- [Member 1 Name]
- [Member 2 Name]
- [Member 3 Name]

## How to Set Up and Run

### 1. Database Setup
1. Start XAMPP and make sure **Apache** and **MySQL** are running.
2. Open **phpMyAdmin** at http://localhost/phpmyadmin
3. Click **Import**, choose `event_management_db.sql`, and click **Go**.
4. The database `event_management_db` will be created with all tables and a default admin account.

**Default login credentials:**
- Username: `admin`
- Password: `admin123`

### 2. NetBeans Project Setup
1. Open **NetBeans IDE**.
2. Go to **File → Open Project** and select the `EventSystem` folder.
3. Right-click the project → **Properties → Libraries → Add JAR/Folder**.
4. Add `mysql-connector-j-X.X.X.jar` from the `lib/` folder (download from https://dev.mysql.com/downloads/connector/j/).
5. Set `LoginFrame.java` as the **Main Class** (Right-click project → Properties → Run → Main Class → `eventsystem.LoginFrame`).

### 3. Run the Application
1. Make sure XAMPP MySQL is running.
2. Press **F6** or click **Run Project** in NetBeans.
3. Login with `admin` / `admin123`.

## Database Design

### Tables
- **users** – admin login credentials (username, hashed password, full_name)
- **events** – event records (event_name, description, event_date, venue, max_slots)
- **registrations** – attendee registrations linked to events via foreign key; includes attendance_status ENUM('Pending','Present','Absent')

### Key Constraints
- `registrations.event_id` → FOREIGN KEY → `events.event_id` ON DELETE CASCADE
- UNIQUE(event_id, email) — prevents duplicate registration per event
- attendance_status uses ENUM to restrict values to Pending, Present, or Absent
