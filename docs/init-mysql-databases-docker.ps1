# =====================================================
# PLM System - MySQL Database Setup (Docker - PowerShell)
# =====================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PLM System - MySQL Database Setup (Docker)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "This script will create the required databases and user for the PLM system."
Write-Host "Using Docker to execute commands on MySQL container."
Write-Host ""

# Prompt for MySQL root password
$MYSQL_ROOT_PASSWORD = Read-Host "Enter MySQL root password (default: root)"
if ([string]::IsNullOrEmpty($MYSQL_ROOT_PASSWORD)) {
    $MYSQL_ROOT_PASSWORD = "root"
}

# Prompt for Docker container name
$MYSQL_CONTAINER = Read-Host "Enter MySQL container name (default: mysql-plm)"
if ([string]::IsNullOrEmpty($MYSQL_CONTAINER)) {
    $MYSQL_CONTAINER = "mysql-plm"
}

Write-Host ""
Write-Host "Using container: $MYSQL_CONTAINER" -ForegroundColor Yellow
Write-Host ""

# Check if container is running
$containerRunning = docker ps --filter "name=$MYSQL_CONTAINER" --format "{{.Names}}" | Select-String -Pattern $MYSQL_CONTAINER

if (-not $containerRunning) {
    Write-Host "ERROR: Container '$MYSQL_CONTAINER' is not running!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please start MySQL container first:" -ForegroundColor Yellow
    Write-Host "  docker run -d -p 3306:3306 --name mysql-plm -e MYSQL_ROOT_PASSWORD=root mysql:8.0"
    Write-Host ""
    Write-Host "Or check running containers:" -ForegroundColor Yellow
    Write-Host "  docker ps"
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Creating databases and user in Docker container..." -ForegroundColor Yellow
Write-Host ""

# Execute SQL commands via Docker
Get-Content init-mysql-databases.sql | docker exec -i $MYSQL_CONTAINER mysql -uroot -p$MYSQL_ROOT_PASSWORD

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
    Write-Host "  1. Container '$MYSQL_CONTAINER' is running"
    Write-Host "  2. Root password is correct"
    Write-Host "  3. MySQL is accessible on port 3306"
    Write-Host ""
    Write-Host "Verify with:" -ForegroundColor Yellow
    Write-Host "  docker ps"
    Write-Host "  docker exec -it $MYSQL_CONTAINER mysql -uroot -p$MYSQL_ROOT_PASSWORD -e `"SHOW DATABASES;`""
    Write-Host ""
}

Write-Host "Press any key to continue..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")





