@echo off
REM =====================================================
REM Initialize MySQL databases for PLM system
REM =====================================================

echo ========================================
echo PLM System - MySQL Database Setup
echo ========================================
echo.
echo This script will create the required databases and user for the PLM system.
echo Make sure MySQL is running on localhost:3306
echo.

REM Prompt for MySQL root password
set /p MYSQL_ROOT_PASSWORD="Enter MySQL root password: "

echo.
echo Creating databases and user...
echo.

REM Execute the SQL script
mysql -h localhost -P 3306 -u root -p%MYSQL_ROOT_PASSWORD% < init-mysql-databases.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS! Databases created successfully
    echo ========================================
    echo.
    echo Created databases:
    echo   - plm_user_db
    echo   - plm_bom_db
    echo   - plm_document_db
    echo   - plm_task_db
    echo   - plm_change_db
    echo.
    echo Created user:
    echo   - Username: plm_user
    echo   - Password: plm_password
    echo.
    echo You can now start the services with MySQL profile.
    echo.
) else (
    echo.
    echo ========================================
    echo ERROR: Failed to create databases
    echo ========================================
    echo Please check:
    echo   1. MySQL is running
    echo   2. Root password is correct
    echo   3. MySQL is accessible on port 3306
    echo.
)

pause





