@echo off
REM =====================================================
REM Initialize MySQL databases for PLM system (Docker)
REM =====================================================

echo ========================================
echo PLM System - MySQL Database Setup (Docker)
echo ========================================
echo.
echo This script will create the required databases and user for the PLM system.
echo Using Docker to execute commands on MySQL container.
echo.

REM Prompt for MySQL root password
set /p MYSQL_ROOT_PASSWORD="Enter MySQL root password (default: root): "
if "%MYSQL_ROOT_PASSWORD%"=="" set MYSQL_ROOT_PASSWORD=root

REM Prompt for Docker container name
set /p MYSQL_CONTAINER="Enter MySQL container name (default: mysql-plm): "
if "%MYSQL_CONTAINER%"=="" set MYSQL_CONTAINER=mysql-plm

echo.
echo Using container: %MYSQL_CONTAINER%
echo.

REM Check if container is running
docker ps --filter "name=%MYSQL_CONTAINER%" --format "{{.Names}}" | findstr /C:"%MYSQL_CONTAINER%" >nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Container '%MYSQL_CONTAINER%' is not running!
    echo.
    echo Please start MySQL container first:
    echo   docker run -d -p 3306:3306 --name mysql-plm -e MYSQL_ROOT_PASSWORD=root mysql:8.0
    echo.
    echo Or check running containers:
    echo   docker ps
    echo.
    pause
    exit /b 1
)

echo Creating databases and user in Docker container...
echo.

REM Execute SQL commands via Docker
docker exec -i %MYSQL_CONTAINER% mysql -uroot -p%MYSQL_ROOT_PASSWORD% < init-mysql-databases.sql

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
    echo   1. Container '%MYSQL_CONTAINER%' is running
    echo   2. Root password is correct
    echo   3. MySQL is accessible on port 3306
    echo.
    echo Verify with:
    echo   docker ps
    echo   docker exec -it %MYSQL_CONTAINER% mysql -uroot -p%MYSQL_ROOT_PASSWORD% -e "SHOW DATABASES;"
    echo.
)

pause





