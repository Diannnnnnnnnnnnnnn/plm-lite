@echo off
REM =====================================================
REM Clean All Data for Production Deployment
REM This will DELETE ALL DATA from MySQL, MinIO, Elasticsearch, and Neo4j
REM =====================================================

echo ========================================
echo PLM SYSTEM - PRODUCTION DATA CLEANUP
echo ========================================
echo.
echo WARNING: This will DELETE ALL DATA from:
echo   - MySQL (all databases)
echo   - MinIO (all objects)
echo   - Elasticsearch (all indices)
echo   - Neo4j (all graph data)
echo.
echo This action CANNOT be undone!
echo.
set /p CONFIRM="Are you sure you want to proceed? (type YES to confirm): "

if /i not "%CONFIRM%"=="YES" (
    echo.
    echo Cleanup cancelled.
    pause
    exit /b 0
)

echo.
echo ========================================
echo Starting cleanup process...
echo ========================================
echo.

REM Stop all services first
echo [1/6] Stopping all services...
call stop-all-services.bat
timeout /t 5 /nobreak > nul

REM Clean MySQL
echo.
echo [2/6] Cleaning MySQL data...
call cleanup-mysql-data.bat

REM Clean MinIO
echo.
echo [3/6] Cleaning MinIO data...
call cleanup-minio-data.bat

REM Clean Elasticsearch
echo.
echo [4/6] Cleaning Elasticsearch data...
call cleanup-elasticsearch-data.bat

REM Clean Neo4j
echo.
echo [5/6] Cleaning Neo4j data...
call cleanup-neo4j-data.bat

REM Clean local files
echo.
echo [6/6] Cleaning local file system data...
echo YES | call cleanup-local-files.bat

echo.
echo ========================================
echo CLEANUP COMPLETE!
echo ========================================
echo.
echo All data has been removed from:
echo   ✓ MySQL
echo   ✓ MinIO
echo   ✓ Elasticsearch
echo   ✓ Neo4j
echo   ✓ Local files (temp uploads, local DBs)
echo.
echo Next steps for production:
echo   1. Review application.yml/properties files for production settings
echo   2. Update security credentials (database passwords, etc.)
echo   3. Start infrastructure: docker-compose -f infra/docker-compose-infrastructure.yaml up -d
echo   4. Start Elasticsearch: docker-compose -f docker-compose-elasticsearch.yml up -d
echo   5. Start MySQL: start-mysql-docker.bat
echo   6. Initialize databases: init-mysql-databases-docker.bat
echo   7. Start services: start-all-services.bat
echo.
pause

