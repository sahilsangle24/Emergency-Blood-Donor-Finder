# Emergency Blood Donor Finder — Complete Development Package

React 19 + Java 21 Servlets + JDBC + Oracle Database 26ai Free + Apache Tomcat 9.
The project continues the supplied Development Stage 2 codebase; it is not a rewrite.

> **Important status:** Stages 3–5 add packaging, deployment, database-installation safety, integration checks, validation and privacy/security hardening. The source has not been run against your local Oracle database or Windows Tomcat from this environment. The final section separates checks actually run from local checks still required. This application coordinates requests only; hospitals and qualified clinicians must independently verify donor eligibility, blood typing and cross-match.

## 1. Project structure

```text
EmergencyBloodDonorFinder/
├── database/
│   ├── 00_create_app_user.sql       # Optional; run as SYS if EBDF_APP does not exist
│   ├── 00_install_or_upgrade.sql    # Preferred, idempotent install/upgrade script
│   ├── 01_create_schema.sql         # Original fresh-schema reference script
│   ├── 02_stage2_migration.sql      # Safe standalone Stage 2 migration
│   ├── 03_verify_installation.sql   # Verify required tables and reference data
│   └── 04_promote_admin.sql        # Optional; promote one already-registered account
├── frontend/                       # React/Vite application
├── src/main/java/com/ebdf/          # Servlet / service / DAO / model / filters
├── src/main/resources/db.properties
├── src/main/webapp/WEB-INF/         # Tomcat 9 web.xml and context config
├── build-and-package.ps1            # Windows PowerShell build helper
├── build-and-package.bat            # Windows CMD build helper
└── pom.xml                          # Maven WAR configuration
```

## 2. Requirements on Windows

- JDK 21
- Apache Maven 3.9+
- Node.js 22.12+ (Node 22 LTS recommended) and npm
- Apache Tomcat 9 (Tomcat 10+ is not compatible with this `javax.servlet` application)
- Oracle Database 26ai Free with PDB service `FREEPDB1`

Verify in PowerShell:

```powershell
java -version
mvn -version
node --version
npm --version
```

## 3. Oracle setup — run scripts, do not create tables manually

The application expects a dedicated schema named `EBDF_APP` in the `FREEPDB1` PDB. Do not run these scripts while connected to `CDB$ROOT`.

### Step A — create the schema user only if it does not already exist

Open PowerShell or Command Prompt and connect to the PDB as SYSDBA:

```powershell
sqlplus sys@//localhost:1521/FREEPDB1 as sysdba
```

Enter your SYS password when prompted. Then run the included script (replace the path with your extracted project folder):

```sql
@C:\path\to\EmergencyBloodDonorFinder\database\00_create_app_user.sql
```

The script prompts for a strong `EBDF_APP` password. If `EBDF_APP` already exists, it keeps the existing password and reapplies the required schema privileges/quota. Do not share the password in chat or commit it to GitHub.

If the `EBDF_APP` schema user already exists and has the privileges/quota granted, skip Step A.

### Step B — install or upgrade the complete schema

Still in SQL*Plus, connect as the application schema:

```sql
CONNECT EBDF_APP@//localhost:1521/FREEPDB1
```

Enter the `EBDF_APP` password. Then run:

```sql
@C:\path\to\EmergencyBloodDonorFinder\database\00_install_or_upgrade.sql
@C:\path\to\EmergencyBloodDonorFinder\database\03_verify_installation.sql
```

`00_install_or_upgrade.sql` is the preferred single script. It:

- Creates missing application tables in foreign-key dependency order.
- Preserves existing Stage 1 / Stage 2 tables and user data.
- Adds missing reference rows and default settings without overwriting existing administrator settings.
- Creates missing indexes.
- Includes primary keys, foreign keys, unique constraints and check constraints for tables it creates.
- Can be run again safely; it does not drop or truncate application tables.

The tables use Oracle `IDENTITY` columns. Oracle manages their backing sequences automatically, so separate manually-created sequences are not required.

**For an existing Stage 1 database:** run the preferred `00_install_or_upgrade.sql`; it creates the Stage 2 invitation table if missing. The standalone `02_stage2_migration.sql` is also safe to run on a Stage 1 schema if you only need that migration. You do not need to run both.

**Do not run `01_create_schema.sql` on a database that already contains application tables.** It is retained as the original fresh-schema reference; the idempotent `00_install_or_upgrade.sql` is the correct script for fresh and existing databases.

### Step C — verify output

The verification script should show all 12 required tables as `PRESENT`, 8 blood groups, and 27 compatibility pairs. If any table is `MISSING`, or SQL*Plus prints an `ORA-` error, stop and send me the complete error text and the command/script line shown. Do not manually edit tables.

### Step D — create your first administrator (after registering normally)

1. Register a normal account through the application (choose Requester or Donor).
2. Connect to SQL*Plus as `EBDF_APP` and run:

```sql
@C:\path\to\EmergencyBloodDonorFinder\database\04_promote_admin.sql
```

3. Enter the exact email address of the account to promote. The script changes only that matching account. Public registration cannot create an ADMIN account.

## 4. Configure Oracle JDBC credentials

The default JDBC URL is:

```text
jdbc:oracle:thin:@//localhost:1521/FREEPDB1
```

Preferred: set environment variables in the same PowerShell window used to start Tomcat. Replace the password locally; never paste it into chat:

```powershell
$env:EBDF_DB_URL = "jdbc:oracle:thin:@//localhost:1521/FREEPDB1"
$env:EBDF_DB_USERNAME = "EBDF_APP"
$env:EBDF_DB_PASSWORD = "YOUR_LOCAL_EBDF_APP_PASSWORD"
$env:EBDF_DB_DRIVER = "oracle.jdbc.OracleDriver"
```

`src/main/resources/db.properties` contains a `CHANGE_ME` placeholder and intentionally refuses to connect until a real password is supplied. Environment variables take precedence over that file.

## 5. Build and run locally

### Option A — one-command Windows build

From the extracted project root in PowerShell:

```powershell
.\build-and-package.ps1
```

Or from Command Prompt:

```bat
build-and-package.bat
```

The helper performs `npm ci`, frontend lint and API contract checks, Vite production build, then Maven WAR packaging. The React production files are placed under `src/main/webapp/app` and packaged into the same WAR.

### Option B — run frontend and backend separately during development

1. Start Oracle Database and confirm `FREEPDB1` is open.
2. Start Tomcat on port **8086** (or change `EBDF_TOMCAT_ORIGIN` below to the actual port).
3. Set the database environment variables above, then start Tomcat from that same terminal:

```powershell
$env:CATALINA_HOME = "C:\path\to\apache-tomcat-9"
& "$env:CATALINA_HOME\bin\catalina.bat" run
```

4. Open another PowerShell window, go to the project `frontend` folder, and run:

```powershell
cd C:\path\to\EmergencyBloodDonorFinder\frontend
npm ci
npm run check
npm run dev
```

5. Open the Vite URL printed in the terminal (normally `http://localhost:5173`). Vite proxies `/api` requests to `http://localhost:8086/EmergencyBloodDonorFinder/api`, so the browser uses the same-origin proxy. The proxy also rewrites Tomcat's session-cookie path to `/` for the Vite origin, allowing the session cookie to accompany `/api` requests.

If Tomcat is on another port, set this before `npm run dev`:

```powershell
$env:EBDF_TOMCAT_ORIGIN = "http://localhost:8080"
```

If your Tomcat context path is not `EmergencyBloodDonorFinder`, set `EBDF_TOMCAT_CONTEXT` when building the production frontend and update `VITE_API_BASE_URL` in `frontend/.env.production` to match that context.

## 6. Deploy the WAR to Tomcat 9

After a successful build, the artifact is:

```text
target\EmergencyBloodDonorFinder.war
```

Copy it to:

```text
%CATALINA_HOME%\webapps\EmergencyBloodDonorFinder.war
```

Start/restart Tomcat. The packaged React application is served from:

```text
http://localhost:8086/EmergencyBloodDonorFinder/
```

The WAR also contains the `/api/*` servlet endpoints. React Router paths under `/app/*` are forwarded to the SPA entry point. The app is configured for Tomcat 9 (`javax.servlet`); do not deploy this WAR to Tomcat 10/11 without a Jakarta migration.

The session is server-side, 30-minute idle timeout, cookie-only tracking, HTTP-only and SameSite=Lax. Use HTTPS and configure secure cookies before any real public deployment. This is currently an educational/local deployment setup, not a production healthcare hosting certification.

## 7. Implemented application modules

- Registration and login with BCrypt password hashes; public registration is limited to DONOR and REQUESTER.
- Donor profile registration/update, availability and contact-consent controls.
- Donor dashboard with Oracle statistics, compatible open requests, invitation/response history and donation records.
- Requester dashboard with Oracle request counts and request history.
- Compatible donor search based on the database blood compatibility matrix; search results do not reveal phone numbers.
- Request detail access is restricted to the owner/admin or a compatible donor who is available or has an invitation/response. Donors do not receive free-text request details that could contain patient-identifying information.
- Blood request creation, donor accept/reject, requester response tracking and owner-only cancellation.
- Oracle-backed notifications with mark-read, mark-all-read and delete actions.
- Donation history submission with pending admin verification.
- Admin dashboards and record lists, account status management, donor availability, request status management, donation verification, reports and persisted settings.
- Account profile management and server-side session restoration.
- Role-protected React routes and server-side role/ownership checks.

Admin deletion is implemented as non-destructive account/request status management where possible. This avoids breaking foreign-key history and audit trails.

## 8. API endpoints

| Method | Endpoint | Role / purpose |
|---|---|---|
| GET | `/api/health` | Tomcat + Oracle connectivity readiness check (no credentials exposed) |
| POST | `/api/register` | Public DONOR / REQUESTER registration |
| POST | `/api/login`, `/api/logout` | Create / invalidate session |
| GET | `/api/session` | Restore current server-side session |
| GET/PUT | `/api/profile` | Signed-in account profile |
| POST | `/api/donor/register` | Donor creates their own donor profile |
| GET/PUT | `/api/donor/profile` | Donor profile |
| GET/PUT | `/api/donor/dashboard` | Donor statistics / availability |
| GET | `/api/donors/search` | Requester/admin compatible donor search |
| POST | `/api/blood-request` | Requester creates request |
| GET | `/api/blood-requests` | Donor's compatible open requests |
| GET | `/api/my-blood-requests` | Requester's own request history |
| GET | `/api/blood-requests/details` | Authorized request detail |
| POST | `/api/blood-requests/cancel` | Request owner cancels an open request |
| POST | `/api/blood-requests/invite` | Requester invites a compatible donor |
| POST | `/api/donor/respond` | Donor accepts/rejects a request |
| GET | `/api/my-responses`, `/api/requester/responses` | Donor / requester response history |
| GET/POST | `/api/notifications` | Notification read/delete actions |
| GET/POST | `/api/donation-history` | Donor history / pending submission |
| GET | `/api/blood-requests/responses` | Request owner/admin response view |
| GET | `/api/admin/*`, `/api/admin/settings` | Admin lists, stats, reports and settings |
| POST | `/api/admin/action` | Admin account/donor/request/donation status actions |

## 9. Testing and known limits

### Checks performed in the packaging environment

- API route contract check: frontend literal API calls matched servlet mappings.
- SQL installation script static audit: all 12 required table definitions found; all use identity columns where appropriate.
- Project source and configuration review performed against the supplied Stage 2 ZIP.

### Checks blocked or requiring your Windows machine

- `npm ci`, ESLint and Vite production build: package registry DNS/network unavailable in this environment; run on Windows with internet access.
- Maven WAR package: Maven is not installed in this environment; run the included PowerShell/CMD build helper on Windows.
- Full Java dependency compilation: Maven dependencies cannot be fetched here; run `mvn clean package` locally and share any compiler output.
- Oracle SQL/JDBC execution: no Oracle Database or SQL*Plus is available here; you must run the supplied SQL scripts and share output/errors.
- Tomcat deployment, browser workflows and session-cookie end-to-end tests: require your local Windows environment.

### Functional limitations

- Notifications are in-app only; email/SMS/push delivery is not configured.
- Live WebSocket notifications are not implemented; pages refresh data on load or after user actions.
- Donor-to-recipient compatibility is an informational coordination filter, not clinical authorization. No distance/routing or automated medical eligibility determination is claimed.
- Admin account removal is non-destructive status management; no irreversible user deletion is exposed.

## 10. If something fails

Please send:
1. The complete SQL*Plus output including the `ORA-` error and line number, or
2. The complete PowerShell output from `build-and-package.ps1`, or
3. The Tomcat `logs\catalina.<date>.log` lines around the failure, plus the browser Network response for the failing API request.

Do not send your database password. I can use those outputs to diagnose and revise the scripts/configuration and continue the local integration cycle.

### Optional database smoke test classes

The repository retains the original command-line smoke tests under `src/test/java` so they are not packaged into the production WAR. They are not automatically executed by Maven; use the `/api/health` endpoint for the first connection check. The smoke-test classes require valid local database credentials.
