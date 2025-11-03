@echo off
REM =====================================================
REM Start MySQL in Docker for PLM System
REM =====================================================

echo ========================================
echo Starting MySQL in Docker
echo ========================================
echo.

REM Check if container already exists
docker ps -a --filter "name=mysql-plm" --format "{{.Names}}" | findstr /C:"mysql-plm" >nul

if %ERRORLEVEL% EQU 0 (
    echo MySQL container 'mysql-plm' already exists.
    echo Checking if it's running...
    
    docker ps --filter "name=mysql-plm" --format "{{.Names}}" | findstr /C:"mysql-plm" >nul
    
    if %ERRORLEVEL% EQU 0 (
        echo Container is already running!
    ) else (
        echo Starting existing container...
        docker start mysql-plm
    )
) else (
    echo Creating new MySQL container...
    echo.
    docker run -d ^
        -p 3306:3306 ^
        --name mysql-plm ^
        -e MYSQL_ROOT_PASSWORD=root ^
        mysql:8.0
    
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo MySQL container created successfully!
        echo Waiting 20 seconds for MySQL to initialize...
        timeout /t 20 /nobreak
    ) else (
        echo.
        echo ERROR: Failed to create MySQL container!
        pause
        exit /b 1
    )
)

echo.
echo ========================================
echo MySQL is running!
echo ========================================
echo.
echo Connection details:
echo   Host: localhost
echo   Port: 3306
echo   Root Password: root
echo.
echo Container name: mysql-plm
echo.
echo Next steps:
echo   1. Run: init-mysql-databases-docker.bat
echo   2. Then: start-all-services.bat
echo.
echo Useful commands:
echo   - View logs:    docker logs mysql-plm
echo   - Stop MySQL:   docker stop mysql-plm
echo   - Start MySQL:  docker start mysql-plm
echo   - Remove MySQL: docker rm -f mysql-plm
echo.
pause





