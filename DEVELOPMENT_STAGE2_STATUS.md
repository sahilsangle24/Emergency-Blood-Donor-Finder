# Development Stage 2 Status

## Implemented
- Requester-owned Oracle blood request history and requester dashboard statistics.
- Oracle-backed donor search using the blood compatibility matrix, active account status, availability and donor contact consent.
- Requester-to-donor invitation records (`PENDING`), donor accept/reject workflow, transactional request matching, and notifications.
- Donor response history and requester-side response tracking. Donor phone is returned only for accepted responses and when consent is enabled.
- Oracle-backed notifications: list, mark one/all read, and delete.
- Donor/admin donation history retrieval; donor submissions remain pending verification.
- Admin dashboard statistics, user/donor/request/donation lists, status updates, report aggregates and persisted settings.
- Settings enforcement for registration, new blood requests and emergency alert notification fan-out.
- Server-side session restoration and shared account profile update.
- Role-protected React routes; requester/donor/admin pages replaced demo data with API calls.
- Fresh-install schema and Stage 1 migration script.

## Verification actually executed
| Check | Result |
|---|---|
| React ESLint (`npm run lint`) | PASS |
| Java compilation (`javac 21`, all `src/main/java`) | PASS |
| Servlet mapping audit | PASS — no duplicate URL patterns |
| Frontend API path vs servlet mapping audit | PASS |
| Blood group seed audit | PASS — 8 blood groups |
| Compatibility matrix static audit | PASS — 27 pairs cover all 8 recipient groups |
| React production build | BLOCKED — Linux native Rolldown binding unavailable in supplied Windows dependency tree |
| Maven WAR packaging | NOT RUN — Maven unavailable in inspection environment |
| Oracle SQL/JDBC execution | NOT RUN — no Oracle/SQL*Plus runtime available |
| Tomcat/browser end-to-end workflows | NOT RUN — runtime unavailable |

## Required Windows verification
1. Fresh install: run `database/01_create_schema.sql`. Existing Stage 1 database: run `database/02_stage2_migration.sql` once instead.
2. Configure Oracle `FREEPDB1` credentials in `db.properties` or `EBDF_DB_*` environment variables.
3. Run `mvn clean package`, deploy to Tomcat 9, and verify port/context path.
4. Run `npm ci`, `npm run lint`, and `npm run build` in `frontend`.
5. Exercise all requester, donor and admin workflows with real Oracle records.

## Limits
- This is not production-certified. Oracle SQL and full end-to-end workflows were not executed in this environment.
- Email delivery is not implemented; notifications are in-app database records.
- Admin management uses controlled status changes/soft deactivation rather than destructive deletion.
- Blood compatibility is a coordination aid only; hospital staff must perform donor eligibility checks, blood typing and cross-matching.
