@echo off
echo ========================================
echo Finding MySQL Password
echo ========================================
echo.
echo Testing common passwords on mysql-container...
echo.

echo Testing: (empty password)
docker exec -i mysql-container mysql -uroot -e "SELECT 'SUCCESS - No password needed!' AS Result;" 2>nul
if %ERRORLEVEL% EQU 0 (
    echo *** PASSWORD IS EMPTY ***
    echo Run: init-mysql-custom.bat and just press Enter
    pause
    exit /b 0
)

echo Testing: root
docker exec -i mysql-container mysql -uroot -proot -e "SELECT 'SUCCESS - Password is: root' AS Result;" 2>nul
if %ERRORLEVEL% EQU 0 (
    echo *** PASSWORD IS: root ***
    pause
    exit /b 0
)

echo Testing: password
docker exec -i mysql-container mysql -uroot -ppassword -e "SELECT 'SUCCESS - Password is: password' AS Result;" 2>nul
if %ERRORLEVEL% EQU 0 (
    echo *** PASSWORD IS: password ***
    pause
    exit /b 0
)

echo Testing: mysql
docker exec -i mysql-container mysql -uroot -pmysql -e "SELECT 'SUCCESS - Password is: mysql' AS Result;" 2>nul
if %ERRORLEVEL% EQU 0 (
    echo *** PASSWORD IS: mysql ***
    pause
    exit /b 0
)

echo Testing: admin
docker exec -i mysql-container mysql -uroot -padmin -e "SELECT 'SUCCESS - Password is: admin' AS Result;" 2>nul
if %ERRORLEVEL% EQU 0 (
    echo *** PASSWORD IS: admin ***
    pause
    exit /b 0
)

echo.
echo ========================================
echo Could not find password
echo ========================================
echo.
echo The password is not one of the common ones.
echo.
echo To find it:
echo   1. Check how you created the container
echo   2. Look for: -e MYSQL_ROOT_PASSWORD=your_password
echo   3. Or check: docker inspect mysql-container
echo.
echo Alternative: Create a new MySQL container with known password:
echo   docker stop mysql-container
echo   docker rm mysql-container
echo   docker run -d -p 3306:3306 --name mysql-container -e MYSQL_ROOT_PASSWORD=root mysql:8.0
echo.
pause





