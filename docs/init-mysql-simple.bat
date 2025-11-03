@echo off
echo ========================================
echo Initializing MySQL Databases (Simple)
echo ========================================
echo.
echo Using container: mysql-container
echo Password: root
echo.

type init-mysql-databases.sql | docker exec -i mysql-container mysql -uroot -proot

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
    echo Verifying...
    docker exec -i mysql-container mysql -uroot -proot -e "SHOW DATABASES LIKE 'plm%%';"
    echo.
) else (
    echo.
    echo ERROR: Failed to create databases
    echo.
)

pause





