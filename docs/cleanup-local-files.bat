@echo off
REM =====================================================
REM Clean Local File System Data
REM This will delete local database files and temp uploads
REM =====================================================

echo ========================================
echo Cleaning Local File System Data
echo ========================================
echo.

echo This will delete:
echo   - data/taskdb.mv.db (Task service local DB)
echo   - document-service/data/documentdb.mv.db (Document service local DB)
echo   - document-service/temp-uploads/* (All temporary uploaded files)
echo.
set /p CONFIRM="Are you sure you want to proceed? (type YES to confirm): "

if /i not "%CONFIRM%"=="YES" (
    echo.
    echo Cleanup cancelled.
    exit /b 0
)

echo.
echo Deleting local database files...

REM Delete task service local database
if exist "data\taskdb.mv.db" (
    del /F /Q "data\taskdb.mv.db"
    echo   ✓ Deleted: data\taskdb.mv.db
) else (
    echo   - Not found: data\taskdb.mv.db
)

REM Delete document service local database
if exist "document-service\data\documentdb.mv.db" (
    del /F /Q "document-service\data\documentdb.mv.db"
    echo   ✓ Deleted: document-service\data\documentdb.mv.db
) else (
    echo   - Not found: document-service\data\documentdb.mv.db
)

echo.
echo Deleting temporary uploaded files...

REM Delete all files in temp-uploads directory
if exist "document-service\temp-uploads\*.*" (
    del /F /Q "document-service\temp-uploads\*.*" 2>nul
    echo   ✓ Deleted all files in document-service\temp-uploads\
) else (
    echo   - Directory is already empty or doesn't exist
)

echo.
echo ========================================
echo Local file cleanup complete!
echo ========================================
echo.


