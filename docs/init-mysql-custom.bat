@echo off
echo ========================================
echo MySQL Database Initialization
echo ========================================
echo.
echo Using container: mysql-container
echo.

set /p MYSQL_PASSWORD="Enter MySQL root password: "

echo.
echo Attempting to initialize databases...
echo.

type init-mysql-databases.sql | docker exec -i mysql-container mysql -uroot -p%MYSQL_PASSWORD%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS! Databases created!
    echo ========================================
    echo.
    echo Created databases:
    echo   - plm_user_db
    echo   - plm_bom_db
    echo   - plm_document_db
    echo   - plm_task_db
    echo   - plm_change_db
    echo.
    echo Created user: plm_user / plm_password
    echo.
    echo Verifying databases...
    docker exec -i mysql-container mysql -uroot -p%MYSQL_PASSWORD% -e "SHOW DATABASES LIKE 'plm%%';"
    echo.
    echo You can now run: start-all-services.bat
    echo.
) else (
    echo.
    echo ========================================
    echo ERROR: Failed to create databases
    echo ========================================
    echo.
    echo Please check:
    echo   1. Container is running: docker ps
    echo   2. Password is correct
    echo.
    echo Try common passwords:
    echo   - root
    echo   - password
    echo   - mysql
    echo   - (empty - just press Enter)
    echo.
)

pause





