@echo off
REM =====================================================
REM Clean MySQL Data - Drop and Recreate All Databases
REM =====================================================

echo ========================================
echo Cleaning MySQL Data
echo ========================================
echo.

REM Check if MySQL container exists and is running
docker ps --filter "name=mysql-plm" --format "{{.Names}}" | findstr /C:"mysql-plm" >nul

if %ERRORLEVEL% NEQ 0 (
    echo MySQL container 'mysql-plm' is not running.
    echo Starting MySQL container...
    call start-mysql-docker.bat
    timeout /t 10 /nobreak
)

echo Dropping all PLM databases...
echo.

REM Drop all databases
docker exec -i mysql-plm mysql -uroot -proot -e "DROP DATABASE IF EXISTS plm_auth;"
docker exec -i mysql-plm mysql -uroot -proot -e "DROP DATABASE IF EXISTS plm_parts;"
docker exec -i mysql-plm mysql -uroot -proot -e "DROP DATABASE IF EXISTS plm_bom;"
docker exec -i mysql-plm mysql -uroot -proot -e "DROP DATABASE IF EXISTS plm_documents;"
docker exec -i mysql-plm mysql -uroot -proot -e "DROP DATABASE IF EXISTS plm_changes;"
docker exec -i mysql-plm mysql -uroot -proot -e "DROP DATABASE IF EXISTS plm_tasks;"
docker exec -i mysql-plm mysql -uroot -proot -e "DROP DATABASE IF EXISTS plm_workflows;"
docker exec -i mysql-plm mysql -uroot -proot -e "DROP DATABASE IF EXISTS plm_users;"

echo.
echo All MySQL databases dropped successfully!
echo Run init-mysql-databases-docker.bat to recreate empty databases.
echo.


