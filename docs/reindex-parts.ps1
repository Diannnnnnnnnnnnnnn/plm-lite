# ========================================
# Reindex Parts to Elasticsearch
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Reindexing Parts to Elasticsearch" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

try {
    Write-Host "Making POST request to BOM service..." -ForegroundColor Yellow
    
    $response = Invoke-RestMethod -Uri "http://localhost:8089/api/v1/parts/elasticsearch/reindex" -Method POST -ErrorAction Stop
    
    Write-Host ""
    Write-Host "✅ SUCCESS!" -ForegroundColor Green
    Write-Host "$response" -ForegroundColor Green
    Write-Host ""
    
} catch {
    Write-Host ""
    Write-Host "❌ ERROR: Failed to reindex parts" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please ensure:" -ForegroundColor Yellow
    Write-Host "  1. BOM service is running on port 8089" -ForegroundColor Yellow
    Write-Host "  2. Elasticsearch is running on port 9200" -ForegroundColor Yellow
    Write-Host ""
    exit 1
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Reindexing Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan




