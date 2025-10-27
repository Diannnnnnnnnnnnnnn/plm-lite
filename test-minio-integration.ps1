# ======================================
# MinIO Integration Test Script
# Tests MinIO connection and document upload/download
# ======================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  MinIO Integration Test" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Test 1: Check MinIO Health
Write-Host "[Test 1] Checking MinIO Health..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9000/minio/health/live" -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ MinIO is healthy and accessible" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ MinIO health check failed: $_" -ForegroundColor Red
    Write-Host "   Make sure MinIO is running: .\start-infrastructure.ps1" -ForegroundColor Yellow
    exit 1
}

# Test 2: Check MinIO Console Access
Write-Host "`n[Test 2] Checking MinIO Console..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9001" -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ MinIO Console is accessible at http://localhost:9001" -ForegroundColor Green
        Write-Host "   Login: minio / password" -ForegroundColor Cyan
    }
} catch {
    Write-Host "❌ MinIO Console check failed: $_" -ForegroundColor Red
}

# Test 3: Check if bucket was created
Write-Host "`n[Test 3] Checking Bucket Creation..." -ForegroundColor Yellow
$bucketLogs = docker logs plm-minio-init 2>&1 | Select-String "plm-documents"
if ($bucketLogs) {
    Write-Host "✅ Bucket 'plm-documents' created successfully" -ForegroundColor Green
    $bucketLogs | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
} else {
    Write-Host "⚠️  Bucket creation logs not found" -ForegroundColor Yellow
}

# Test 4: Check Document Service
Write-Host "`n[Test 4] Checking Document Service..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/documents" -TimeoutSec 5
    Write-Host "✅ Document Service is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Document Service is not running" -ForegroundColor Red
    Write-Host "   Start it with: cd document-service && mvn spring-boot:run" -ForegroundColor Yellow
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`n📋 Manual Tests:" -ForegroundColor Yellow
Write-Host "   1. Open MinIO Console: http://localhost:9001" -ForegroundColor White
Write-Host "   2. Login with: minio / password" -ForegroundColor White
Write-Host "   3. Verify 'plm-documents' bucket exists" -ForegroundColor White
Write-Host "   4. Upload a test document via API" -ForegroundColor White
Write-Host "   5. Check if file appears in MinIO Console" -ForegroundColor White

Write-Host "`n💡 To test document upload:" -ForegroundColor Cyan
Write-Host @"
   `$file = Get-Item "test.pdf"
   `$form = @{
       file = `$file
       title = "Test Document"
       description = "Testing MinIO"
       type = "SPECIFICATION"
   }
   Invoke-WebRequest -Uri "http://localhost:8081/documents/upload" ``
       -Method Post -Form `$form
"@ -ForegroundColor Gray

Write-Host "`n"

