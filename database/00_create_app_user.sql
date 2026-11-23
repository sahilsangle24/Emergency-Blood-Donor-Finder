-- Optional one-time setup. Run as SYS in the FREEPDB1 PDB, not as EBDF_APP.
-- SQL*Plus prompts for the password. Use a strong local password and keep it private.
SET DEFINE ON
ACCEPT app_password CHAR PROMPT 'Enter a strong password for EBDF_APP: ' HIDE
SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
DECLARE
  n NUMBER;
  pwd VARCHAR2(128) := '&app_password';
BEGIN
  SELECT COUNT(*) INTO n FROM DBA_USERS WHERE USERNAME='EBDF_APP';
  IF n=0 THEN
    EXECUTE IMMEDIATE 'CREATE USER EBDF_APP IDENTIFIED BY "' || REPLACE(pwd,'"','""') || '"';
    DBMS_OUTPUT.PUT_LINE('Created EBDF_APP');
  ELSE
    DBMS_OUTPUT.PUT_LINE('EBDF_APP already exists; password was not changed');
  END IF;
  EXECUTE IMMEDIATE 'GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE VIEW, CREATE PROCEDURE, CREATE TRIGGER TO EBDF_APP';
  EXECUTE IMMEDIATE 'ALTER USER EBDF_APP QUOTA UNLIMITED ON USERS';
END;
/
UNDEFINE app_password
PROMPT Application schema user is ready. Connect as EBDF_APP to FREEPDB1 before running 00_install_or_upgrade.sql.
