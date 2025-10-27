# ======================================
# MinIO Issue Diagnostic Script
# ======================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  MinIO Integration Diagnostics" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$baseUrl = "http://localhost:8081/api/v1/documents"
$hasIssues = $false

# Check 1: Is the service running?
Write-Host "[Check 1] Document Service Status..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/health/minio" -TimeoutSec 5
    Write-Host "✅ Service is running" -ForegroundColor Green
    Write-Host "   MinIO Status: $($health.status)" -ForegroundColor White
} catch {
    Write-Host "❌ Service is NOT running!" -ForegroundColor Red
    Write-Host "   Start it with: cd document-service && mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

# Check 2: Check if attachFileWithMetadata method exists
Write-Host "`n[Check 2] Checking if code changes are compiled..." -ForegroundColor Yellow
$implFile = "document-service\src\main\java\com\example\document_service\service\impl\DocumentServiceImpl.java"
$hasNewMethod = Select-String -Path $implFile -Pattern "attachFileWithMetadata" -Quiet
if ($hasNewMethod) {
    Write-Host "✅ New method exists in source code" -ForegroundColor Green
} else {
    Write-Host "❌ New method NOT found in source code!" -ForegroundColor Red
    $hasIssues = $true
}

# Check 3: Test actual upload
Write-Host "`n[Check 3] Testing document upload..." -ForegroundColor Yellow

# Create test file
"Test content for diagnostics" | Out-File -FilePath "diagnostic-test.txt" -Encoding UTF8

# Create document first
$createBody = @{
    title = "Diagnostic Test"
    creator = "DiagnosticUser"
    stage = "CONCEPTUAL_DESIGN"
    category = "Test"
} | ConvertTo-Json

try {
    $doc = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $createBody -ContentType "application/json"
    Write-Host "✅ Document created: $($doc.id)" -ForegroundColor Green
    $docId = $doc.id
} catch {
    Write-Host "❌ Failed to create document: $_" -ForegroundColor Red
    $hasIssues = $true
    Remove-Item "diagnostic-test.txt" -ErrorAction SilentlyContinue
    exit 1
}

# Upload file
Write-Host "`n[Check 4] Uploading file..." -ForegroundColor Yellow
try {
    $file = Get-Item "diagnostic-test.txt"
    $form = @{
        file = $file
        user = "DiagnosticUser"
    }
    
    $uploadResult = Invoke-RestMethod -Uri "$baseUrl/$docId/upload" -Method Post -Form $form
    Write-Host "✅ File uploaded: $uploadResult" -ForegroundColor Green
} catch {
    Write-Host "❌ Upload failed: $_" -ForegroundColor Red
    $hasIssues = $true
}

Start-Sleep -Seconds 1

# Check 5: Verify metadata was saved
Write-Host "`n[Check 5] Checking if metadata was saved..." -ForegroundColor Yellow
try {
    $docInfo = Invoke-RestMethod -Uri "$baseUrl/$docId" -Method Get
    
    Write-Host "Document Info:" -ForegroundColor Cyan
    Write-Host "  FileKey: $($docInfo.fileKey)" -ForegroundColor White
    Write-Host "  OriginalFilename: $($docInfo.originalFilename)" -ForegroundColor White
    Write-Host "  ContentType: $($docInfo.contentType)" -ForegroundColor White
    Write-Host "  FileSize: $($docInfo.fileSize)" -ForegroundColor White
    Write-Host "  StorageLocation: $($docInfo.storageLocation)" -ForegroundColor White
    
    if ($null -ne $docInfo.originalFilename -and $docInfo.originalFilename -ne "") {
        Write-Host "✅ Metadata IS saved!" -ForegroundColor Green
    } else {
        Write-Host "❌ Metadata NOT saved - originalFilename is null/empty" -ForegroundColor Red
        Write-Host "   This means the service is using OLD CODE" -ForegroundColor Yellow
        Write-Host "   ACTION REQUIRED: Restart the service!" -ForegroundColor Yellow
        $hasIssues = $true
    }
} catch {
    Write-Host "❌ Failed to get document info: $_" -ForegroundColor Red
    $hasIssues = $true
}

# Check 6: Test file/info endpoint
Write-Host "`n[Check 6] Testing file/info endpoint..." -ForegroundColor Yellow
try {
    $fileInfo = Invoke-RestMethod -Uri "$baseUrl/$docId/file/info" -Method Get
    Write-Host "✅ File info endpoint works!" -ForegroundColor Green
    Write-Host "  Exists: $($fileInfo.exists)" -ForegroundColor White
    Write-Host "  File Size: $($fileInfo.fileSize)" -ForegroundColor White
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 404) {
        Write-Host "❌ File info returned 404 - No metadata found" -ForegroundColor Red
        Write-Host "   This confirms the service needs to be RESTARTED" -ForegroundColor Yellow
        $hasIssues = $true
    } else {
        Write-Host "❌ File info failed: $_" -ForegroundColor Red
        $hasIssues = $true
    }
}

# Check 7: Test exists endpoint
Write-Host "`n[Check 7] Testing file/exists endpoint..." -ForegroundColor Yellow
try {
    $exists = Invoke-RestMethod -Uri "$baseUrl/$docId/file/exists" -Method Get
    if ($exists) {
        Write-Host "✅ File exists in storage" -ForegroundColor Green
    } else {
        Write-Host "❌ File does NOT exist in storage" -ForegroundColor Red
        $hasIssues = $true
    }
} catch {
    Write-Host "❌ Exists check failed: $_" -ForegroundColor Red
    $hasIssues = $true
}

# Check 8: Test delete endpoint
Write-Host "`n[Check 8] Testing delete endpoint..." -ForegroundColor Yellow
try {
    $deleteResult = Invoke-RestMethod -Uri "$baseUrl/$docId/file" -Method Delete
    Write-Host "✅ Delete endpoint responded: $deleteResult" -ForegroundColor Green
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 404) {
        Write-Host "❌ Delete returned 404 - No file found" -ForegroundColor Red
        Write-Host "   This is because metadata wasn't saved during upload" -ForegroundColor Yellow
        $hasIssues = $true
    } else {
        Write-Host "❌ Delete failed: $_" -ForegroundColor Red
        $hasIssues = $true
    }
}

# Cleanup
Remove-Item "diagnostic-test.txt" -ErrorAction SilentlyContinue

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Diagnostic Summary" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

if ($hasIssues) {
    Write-Host "❌ ISSUES DETECTED!" -ForegroundColor Red
    Write-Host "`n🔧 SOLUTION:" -ForegroundColor Yellow
    Write-Host "   1. Stop the current document-service (Ctrl+C)" -ForegroundColor White
    Write-Host "   2. Recompile and restart:" -ForegroundColor White
    Write-Host "      cd document-service" -ForegroundColor Cyan
    Write-Host "      mvn clean compile" -ForegroundColor Cyan
    Write-Host "      mvn spring-boot:run" -ForegroundColor Cyan
    Write-Host "`n   3. Wait for service to fully start" -ForegroundColor White
    Write-Host "   4. Run this diagnostic again: .\diagnose-minio-issue.ps1" -ForegroundColor White
    Write-Host "`n   5. If still failing, check service logs for errors" -ForegroundColor White
} else {
    Write-Host "✅ ALL CHECKS PASSED!" -ForegroundColor Green
    Write-Host "   The MinIO integration is working correctly." -ForegroundColor White
}

Write-Host ""

