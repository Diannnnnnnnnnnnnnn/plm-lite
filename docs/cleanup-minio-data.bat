@echo off
REM =====================================================
REM Clean MinIO Data - Remove All Objects
REM =====================================================

echo ========================================
echo Cleaning MinIO Data
echo ========================================
echo.

REM Check if MinIO is running
docker ps --filter "name=plm-minio" --format "{{.Names}}" | findstr /C:"plm-minio" >nul

if %ERRORLEVEL% NEQ 0 (
    echo MinIO container 'plm-minio' is not running.
    echo Please start infrastructure services first:
    echo   docker-compose -f infra/docker-compose-infrastructure.yaml up -d
    exit /b 1
)

echo Removing all objects from MinIO bucket 'plm-documents'...
echo.

REM Remove all objects from the bucket using MinIO client
docker run --rm --network plm-lite_plm-network ^
    -e MC_HOST_plmminio=http://minio:password@plm-minio:9000 ^
    minio/mc ^
    rm --recursive --force plmminio/plm-documents/

echo.
echo All MinIO objects removed successfully!
echo Bucket 'plm-documents' is now empty.
echo.


