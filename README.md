
# 🩸 Emergency Blood Donor Finder

A full-stack web application designed to connect people in need of blood with compatible blood donors during emergencies.

## 🚀 Project Overview

Emergency Blood Donor Finder helps requesters search for compatible blood donors, create emergency blood requests, and track donor responses. Donors can manage their profiles, availability, invitations, and donation history. Administrators can manage users, requests, donations, and application settings.

## 🛠️ Tech Stack

**Frontend**
- React.js 19
- JavaScript
- HTML5
- CSS3
- Vite

**Backend**
- Core Java 21
- Java Servlets
- JDBC
- MVC Architecture

**Database**
- Oracle Database 26ai Free

**Server & Tools**
- Apache Tomcat 9
- Apache Maven
- Git & GitHub
- Eclipse IDE
- VS Code

## ✨ Features

### 👤 Authentication
- User Registration
- User Login and Logout
- Role-Based Access
- Secure Password Hashing

### 🩸 Donor Module
- Donor Profile Management
- Update Blood Group and Location
- Manage Availability
- View Blood Requests
- Accept or Reject Requests
- Donation History

### 🏥 Requester Module
- Requester Dashboard
- Search Compatible Blood Donors
- Create Emergency Blood Requests
- Send Donor Invitations
- Track Request Status
- View Donor Responses

### 🛡️ Admin Module
- Admin Dashboard
- Manage Users
- Manage Donors
- Manage Blood Requests
- Verify Donation History
- Manage Application Settings
- View Reports and Statistics

### 🔔 Additional Features
- In-App Notifications
- Blood Group Compatibility
- Request Tracking
- Session Management
- Protected Routes

## 📸 Screenshots

### Application Overview
![Application Overview](screenshots/00-all-pages-overview.png)

### Login & Registration
![Login and Registration](screenshots/01-login-registration.png)

### Donor Dashboard
![Donor Dashboard](screenshots/02-donor-dashboard.png)

### Requester Dashboard
![Requester Dashboard](screenshots/03-requester-dashboard.png)

### Admin Dashboard
![Admin Dashboard](screenshots/04-admin-dashboard.png)

### Donor Search
![Donor Search](screenshots/05-donor-search.png)

### Blood Request Form
![Blood Request Form](screenshots/06-blood-request-form.png)

*Screenshots are illustrative UI mockups with sample data, not verified live application captures.*

## 📂 Project Structure

```text
EmergencyBloodDonorFinder/
│
├── database/
│   ├── 00_create_app_user.sql
│   ├── 00_install_or_upgrade.sql
│   ├── 01_create_schema.sql
│   ├── 02_stage2_migration.sql
│   ├── 03_verify_installation.sql
│   └── 04_promote_admin.sql
│
├── frontend/
│   └── React Application
│
├── src/
│   └── main/
│       ├── java/com/ebdf/
│       │   ├── controller/
│       │   ├── dao/
│       │   ├── model/
│       │   ├── service/
│       │   └── filter/
│       │
│       ├── resources/
│       │   └── db.properties
│       │
│       └── webapp/
│           └── WEB-INF/
│
├── screenshots/
├── pom.xml
├── build-and-package.bat
├── build-and-package.ps1
└── README.md
```

## ⚙️ Installation & Setup

### 1. Prerequisites

Install the following:

- JDK 21
- Node.js and npm
- Apache Maven
- Oracle Database 26ai Free
- Apache Tomcat 9
- Eclipse IDE

### 2. Clone Repository

```bash
git clone https://github.com/sahilsangle24/Emergency-Blood-Donor-Finder.git
```

Navigate into the project:

```bash
cd Emergency-Blood-Donor-Finder
```

### 3. Configure Oracle Database

Create the application user using the provided SQL script if it does not already exist.

```sql
@database/00_create_app_user.sql
```

Connect to the `FREEPDB1` database as `EBDF_APP`, then execute:

```sql
@database/00_install_or_upgrade.sql
@database/03_verify_installation.sql
```

The database includes tables for users, donors, requesters, blood requests, donor responses, notifications, donation history, and other application data.

### 4. Configure Database Connection

Set the following environment variables in PowerShell:

```powershell
$env:EBDF_DB_URL = "jdbc:oracle:thin:@//localhost:1521/FREEPDB1"
$env:EBDF_DB_USERNAME = "EBDF_APP"
$env:EBDF_DB_PASSWORD = "YOUR_DATABASE_PASSWORD"
$env:EBDF_DB_DRIVER = "oracle.jdbc.OracleDriver"
```

Do not upload your actual database password to GitHub.

### 5. Run Frontend

```bash
cd frontend
npm ci
npm run dev
```

Open the Vite URL displayed in your terminal, normally:

```text
http://localhost:5173
```

### 6. Build Backend

Return to the project root and run:

```powershell
.\build-and-package.ps1
```

Or:

```bash
mvn clean package
```

The generated WAR file is:

```text
target/EmergencyBloodDonorFinder.war
```

### 7. Deploy on Apache Tomcat 9

Copy the WAR file into Tomcat's `webapps` directory.

Start Tomcat and access:

```text
http://localhost:8086/EmergencyBloodDonorFinder/
```

Use the port configured in your Tomcat server.

## 🔗 Backend API Modules

| API Endpoint | Purpose |
|---|---|
| `/api/register` | User Registration |
| `/api/login` | User Login |
| `/api/logout` | User Logout |
| `/api/session` | Session Management |
| `/api/profile` | User Profile |
| `/api/donor/dashboard` | Donor Dashboard |
| `/api/donors/search` | Search Donors |
| `/api/blood-request` | Create Blood Request |
| `/api/blood-requests` | View Blood Requests |
| `/api/donor/respond` | Accept or Reject Request |
| `/api/notifications` | Notifications |
| `/api/donation-history` | Donation History |
| `/api/admin/*` | Admin Operations |
| `/api/health` | Backend and Database Health |

## 🔐 Security

- BCrypt password hashing
- Role-Based Authorization
- Server-Side Session Management
- Protected API Endpoints
- User Ownership Validation
- Secure Session Cookies

## 🧪 Project Testing Status

- Oracle schema installation and verification: Completed locally.
- Maven WAR packaging: Successful.
- React production assets included in WAR: Verified.
- Full Tomcat deployment and end-to-end integration testing: In progress.

## 🎯 Future Enhancements

- Email and SMS Notifications
- Live Donor Location Tracking
- Google Maps Integration
- Real-Time Notifications
- Mobile Application
- Advanced Analytics

## 👨‍💻 Developer

**Sahil Sangale**

GitHub: [@sahilsangle24](https://github.com/sahilsangle24)

Project: [Emergency Blood Donor Finder](https://github.com/sahilsangle24/Emergency-Blood-Donor-Finder)

---

⭐ If you find this project useful, consider giving it a star!
