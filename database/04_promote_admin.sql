-- Optional: promote one existing account to administrator. Run as EBDF_APP.
-- This script changes only the account matching the email you enter.
SET DEFINE ON
ACCEPT admin_email CHAR PROMPT 'Enter the registered account email to promote: '
SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
DECLARE
  n NUMBER;
BEGIN
  UPDATE USERS SET ROLE='ADMIN' WHERE LOWER(EMAIL)=LOWER(TRIM('&admin_email'));
  n := SQL%ROWCOUNT;
  IF n=1 THEN
    DBMS_OUTPUT.PUT_LINE('Administrator role granted to the selected account.');
  ELSIF n=0 THEN
    DBMS_OUTPUT.PUT_LINE('No account matched that email. No changes made.');
  ELSE
    RAISE_APPLICATION_ERROR(-20001,'More than one account matched; no promotion should be performed.');
  END IF;
END;
/
COMMIT;
UNDEFINE admin_email
