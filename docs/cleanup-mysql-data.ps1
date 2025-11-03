# =====================================================
# Clean MySQL Data - Drop and Recreate All Databases
# =====================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Cleaning MySQL Data" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if MySQL container exists and is running
$mysqlRunning = docker ps --filter "name=mysql-plm" --format "{{.Names}}" | Select-String "mysql-plm"

if (-not $mysqlRunning) {
    Write-Host "MySQL container 'mysql-plm' is not running." -ForegroundColor Yellow
    Write-Host "Starting MySQL container..." -ForegroundColor Yellow
    & "$PSScriptRoot\start-mysql-docker.bat"
    Start-Sleep -Seconds 10
}

Write-Host "Dropping all PLM databases..." -ForegroundColor Yellow
Write-Host ""

# Drop all databases
$databases = @(
    "plm_auth",
    "plm_parts",
    "plm_bom",
    "plm_documents",
    "plm_changes",
    "plm_tasks",
    "plm_workflows",
    "plm_users"
)

foreach ($db in $databases) {
    docker exec mysql-plm mysql -uroot -proot -e "DROP DATABASE IF EXISTS $db;"
    Write-Host "  Dropped: $db" -ForegroundColor Gray
}

Write-Host ""
Write-Host "All MySQL databases dropped successfully!" -ForegroundColor Green
Write-Host "Run init-mysql-databases-docker.bat to recreate empty databases." -ForegroundColor Cyan
Write-Host ""


