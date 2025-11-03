# =====================================================
# Clean Local File System Data
# This will delete local database files and temp uploads
# =====================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Cleaning Local File System Data" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "This will delete:" -ForegroundColor Yellow
Write-Host "  - data/taskdb.mv.db (Task service local DB)" -ForegroundColor Yellow
Write-Host "  - document-service/data/documentdb.mv.db (Document service local DB)" -ForegroundColor Yellow
Write-Host "  - document-service/temp-uploads/* (All temporary uploaded files)" -ForegroundColor Yellow
Write-Host ""

$confirmation = Read-Host "Are you sure you want to proceed? (type YES to confirm)"

if ($confirmation -ne "YES") {
    Write-Host ""
    Write-Host "Cleanup cancelled." -ForegroundColor Green
    exit 0
}

Write-Host ""
Write-Host "Deleting local database files..." -ForegroundColor Yellow

# Delete task service local database
if (Test-Path "data\taskdb.mv.db") {
    Remove-Item -Path "data\taskdb.mv.db" -Force
    Write-Host "  ✓ Deleted: data\taskdb.mv.db" -ForegroundColor Green
} else {
    Write-Host "  - Not found: data\taskdb.mv.db" -ForegroundColor DarkGray
}

# Delete document service local database
if (Test-Path "document-service\data\documentdb.mv.db") {
    Remove-Item -Path "document-service\data\documentdb.mv.db" -Force
    Write-Host "  ✓ Deleted: document-service\data\documentdb.mv.db" -ForegroundColor Green
} else {
    Write-Host "  - Not found: document-service\data\documentdb.mv.db" -ForegroundColor DarkGray
}

Write-Host ""
Write-Host "Deleting temporary uploaded files..." -ForegroundColor Yellow

# Delete all files in temp-uploads directory
if (Test-Path "document-service\temp-uploads") {
    $files = Get-ChildItem -Path "document-service\temp-uploads" -File
    if ($files.Count -gt 0) {
        Remove-Item -Path "document-service\temp-uploads\*.*" -Force -ErrorAction SilentlyContinue
        Write-Host "  ✓ Deleted $($files.Count) files from document-service\temp-uploads\" -ForegroundColor Green
    } else {
        Write-Host "  - Directory is already empty" -ForegroundColor DarkGray
    }
} else {
    Write-Host "  - Directory doesn't exist" -ForegroundColor DarkGray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Local file cleanup complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""


