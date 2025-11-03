# =====================================================
# Clean All Data for Production Deployment (PowerShell)
# This will DELETE ALL DATA from MySQL, MinIO, Elasticsearch, and Neo4j
# =====================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PLM SYSTEM - PRODUCTION DATA CLEANUP" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "WARNING: This will DELETE ALL DATA from:" -ForegroundColor Yellow
Write-Host "  - MySQL (all databases)" -ForegroundColor Yellow
Write-Host "  - MinIO (all objects)" -ForegroundColor Yellow
Write-Host "  - Elasticsearch (all indices)" -ForegroundColor Yellow
Write-Host "  - Neo4j (all graph data)" -ForegroundColor Yellow
Write-Host ""
Write-Host "This action CANNOT be undone!" -ForegroundColor Red
Write-Host ""

$confirmation = Read-Host "Are you sure you want to proceed? (type YES to confirm)"

if ($confirmation -ne "YES") {
    Write-Host ""
    Write-Host "Cleanup cancelled." -ForegroundColor Green
    exit 0
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting cleanup process..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Stop all services first
Write-Host "[1/6] Stopping all services..." -ForegroundColor Yellow
& "$PSScriptRoot\stop-all-services.ps1"
Start-Sleep -Seconds 5

# Clean MySQL
Write-Host ""
Write-Host "[2/6] Cleaning MySQL data..." -ForegroundColor Yellow
& "$PSScriptRoot\cleanup-mysql-data.ps1"

# Clean MinIO
Write-Host ""
Write-Host "[3/6] Cleaning MinIO data..." -ForegroundColor Yellow
& "$PSScriptRoot\cleanup-minio-data.ps1"

# Clean Elasticsearch
Write-Host ""
Write-Host "[4/6] Cleaning Elasticsearch data..." -ForegroundColor Yellow
& "$PSScriptRoot\cleanup-elasticsearch-data.ps1"

# Clean Neo4j
Write-Host ""
Write-Host "[5/6] Cleaning Neo4j data..." -ForegroundColor Yellow
& "$PSScriptRoot\cleanup-neo4j-data.ps1"

# Clean local files
Write-Host ""
Write-Host "[6/6] Cleaning local file system data..." -ForegroundColor Yellow
Write-Host "YES" | & "$PSScriptRoot\cleanup-local-files.ps1"

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "CLEANUP COMPLETE!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "All data has been removed from:" -ForegroundColor Green
Write-Host "  ✓ MySQL" -ForegroundColor Green
Write-Host "  ✓ MinIO" -ForegroundColor Green
Write-Host "  ✓ Elasticsearch" -ForegroundColor Green
Write-Host "  ✓ Neo4j" -ForegroundColor Green
Write-Host "  ✓ Local files (temp uploads, local DBs)" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps for production:" -ForegroundColor Cyan
Write-Host "  1. Review application.yml/properties files for production settings"
Write-Host "  2. Update security credentials (database passwords, etc.)"
Write-Host "  3. Start infrastructure: docker-compose -f infra/docker-compose-infrastructure.yaml up -d"
Write-Host "  4. Start Elasticsearch: docker-compose -f docker-compose-elasticsearch.yml up -d"
Write-Host "  5. Start MySQL: .\start-mysql-docker.bat"
Write-Host "  6. Initialize databases: .\init-mysql-databases-docker.bat"
Write-Host "  7. Start services: .\start-all-services.bat"
Write-Host ""

