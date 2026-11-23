@echo off
setlocal
cd /d "%~dp0frontend" || exit /b 1
call npm ci || exit /b 1
call npm run check || exit /b 1
call npm run build || exit /b 1
cd /d "%~dp0" || exit /b 1
call mvn clean package || exit /b 1
echo WAR created at target
