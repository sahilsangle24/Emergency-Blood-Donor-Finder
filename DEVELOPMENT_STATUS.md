# Development Status — Stages 1–5

The latest source tree is based on the supplied Development Stage 2 ZIP. Development Stages 3–5 have now been applied; see `DEVELOPMENT_STAGE3_5_STATUS.md` for the exact change list and verification matrix.

## Current implementation

- React frontend with role-protected routes and API-backed requester, donor and admin modules.
- Java 21 Servlet/JDBC MVC backend targeting Tomcat 9 (`javax.servlet`).
- Oracle Database schema and DAO integration targeting the `FREEPDB1` PDB.
- Registration/login, donor profile, requester requests, compatible donor search, donor responses, request tracking/cancellation, notifications, donation history, admin operations, settings and reports.
- Same-origin Vite API proxy for development and React assets packaged inside the WAR for production.
- Idempotent Oracle installer/upgrade script, safe Stage 2 migration, verification script and admin promotion helper.
- Server-side session and role/ownership checks, input validation, restricted CORS, privacy controls and health endpoint.

## Current release readiness

**Source/configuration implementation: advanced local-test candidate.**

**Not yet certified as end-to-end runnable:** the build environment did not have npm registry access or Maven, and it had no Oracle Database 26ai / SQL*Plus / Tomcat runtime. Do not treat the project as runtime-tested until the Windows steps in `README.md` pass.

## Next local gate

1. Run `database/00_install_or_upgrade.sql` as `EBDF_APP` in `FREEPDB1`.
2. Run `build-and-package.ps1` on Windows.
3. Deploy the resulting WAR to Tomcat 9 with `EBDF_DB_*` environment variables configured.
4. Confirm `/EmergencyBloodDonorFinder/api/health` returns `database: connected`.
5. Run the end-to-end user workflows and share any failures for the next debugging pass.
