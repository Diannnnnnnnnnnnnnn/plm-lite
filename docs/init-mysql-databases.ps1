# =====================================================
# PLM System - MySQL Database Setup (PowerShell)
# =====================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PLM System - MySQL Database Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "This script will create the required databases and user for the PLM system."
Write-Host "Make sure MySQL is running on localhost:3306"
Write-Host ""

# Prompt for MySQL root password
$MYSQL_ROOT_PASSWORD = Read-Host "Enter MySQL root password" -AsSecureString
$MYSQL_ROOT_PASSWORD_PLAIN = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($MYSQL_ROOT_PASSWORD))

Write-Host ""
Write-Host "Creating databases and user..." -ForegroundColor Yellow
Write-Host ""

# Execute the SQL script
$env:MYSQL_PWD = $MYSQL_ROOT_PASSWORD_PLAIN
mysql -h localhost -P 3306 -u root < init-mysql-databases.sql

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "SUCCESS! Databases created successfully" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Created databases:" -ForegroundColor Green
    Write-Host "  - plm_user_db"
    Write-Host "  - plm_bom_db"
    Write-Host "  - plm_document_db"
    Write-Host "  - plm_task_db"
    Write-Host "  - plm_change_db"
    Write-Host ""
    Write-Host "Created user:" -ForegroundColor Green
    Write-Host "  - Username: plm_user"
    Write-Host "  - Password: plm_password"
    Write-Host ""
    Write-Host "You can now start the services with MySQL profile."
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "ERROR: Failed to create databases" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please check:" -ForegroundColor Yellow
    Write-Host "  1. MySQL is running"
    Write-Host "  2. Root password is correct"
    Write-Host "  3. MySQL is accessible on port 3306"
    Write-Host ""
}

# Clear password from environment
Remove-Item Env:\MYSQL_PWD

Write-Host "Press any key to continue..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")





