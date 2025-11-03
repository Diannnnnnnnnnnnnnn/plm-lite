# =====================================================
# Clean Elasticsearch Data - Delete All Indices
# =====================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Cleaning Elasticsearch Data" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Elasticsearch is running
$esRunning = docker ps --filter "name=plm-elasticsearch" --format "{{.Names}}" | Select-String "plm-elasticsearch"

if (-not $esRunning) {
    Write-Host "Elasticsearch container 'plm-elasticsearch' is not running." -ForegroundColor Red
    Write-Host "Please start Elasticsearch first:" -ForegroundColor Yellow
    Write-Host "  docker-compose -f docker-compose-elasticsearch.yml up -d" -ForegroundColor Yellow
    exit 1
}

Write-Host "Waiting for Elasticsearch to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "Deleting all indices..." -ForegroundColor Yellow
Write-Host ""

# Delete all indices (except system indices)
$indices = @("parts", "documents", "changes", "tasks", "boms")

foreach ($index in $indices) {
    try {
        Invoke-RestMethod -Uri "http://localhost:9200/$index" -Method Delete -ErrorAction SilentlyContinue | Out-Null
        Write-Host "  Deleted: $index index" -ForegroundColor Gray
    } catch {
        Write-Host "  Skipped: $index index (may not exist)" -ForegroundColor DarkGray
    }
}

Write-Host ""
Write-Host "All Elasticsearch indices deleted successfully!" -ForegroundColor Green
Write-Host "Indices will be recreated automatically when services start." -ForegroundColor Cyan
Write-Host ""


