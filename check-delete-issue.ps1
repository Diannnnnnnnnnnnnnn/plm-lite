# Check Delete Issue
Write-Host "Checking delete functionality..." -ForegroundColor Cyan

# 1. Check MinIO is accessible
Write-Host "`n1. Checking MinIO connection..."
try {
    $health = Invoke-RestMethod -Uri "http://localhost:9000/minio/health/live" -TimeoutSec 5
    Write-Host "   MinIO is accessible" -ForegroundColor Green
} catch {
    Write-Host "   MinIO is NOT accessible!" -ForegroundColor Red
}

# 2. Check document service can connect to MinIO
Write-Host "`n2. Checking document-service MinIO health..."
try {
    $docHealth = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/health/minio"
    Write-Host "   Status: $($docHealth.status)" -ForegroundColor $(if ($docHealth.status -eq "UP") {"Green"} else {"Red"})
} catch {
    Write-Host "   Health check failed!" -ForegroundColor Red
}

# 3. Check what MinIO config document-service is using
Write-Host "`n3. Checking application.properties config..."
$config = Get-Content "document-service\src\main\resources\application.properties" | Select-String "minio"
$config | ForEach-Object { Write-Host "   $_" }

# 4. Get a document with a file
Write-Host "`n4. Finding document with file..."
$docs = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents"
$docWithFile = $docs | Where-Object { $null -ne $_.fileKey -and $_.fileKey -ne "" } | Select-Object -First 1

if ($docWithFile) {
    Write-Host "   Found: $($docWithFile.id)" -ForegroundColor Green
    Write-Host "   FileKey: $($docWithFile.fileKey)"
    Write-Host "   StorageLocation: $($docWithFile.storageLocation)"
    
    # 5. Try to check if file exists
    Write-Host "`n5. Checking if file exists in storage..."
    try {
        $exists = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$($docWithFile.id)/file/exists"
        Write-Host "   Exists: $exists" -ForegroundColor $(if ($exists) {"Green"} else {"Red"})
    } catch {
        Write-Host "   Check failed: $_" -ForegroundColor Red
    }
    
    # 6. Try to delete
    Write-Host "`n6. Attempting to delete file..."
    try {
        $deleteResult = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$($docWithFile.id)/file" -Method Delete
        Write-Host "   Result: $deleteResult" -ForegroundColor Green
    } catch {
        Write-Host "   Delete failed!" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)"
        if ($_.Exception.Response) {
            $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "   Response: $responseBody"
        }
    }
    
    # 7. Check logs
    Write-Host "`n7. Check document-service logs for delete errors"
    Write-Host "   Look in the terminal where mvn spring-boot:run is running"
    Write-Host "   Search for: 'Failed to delete file from MinIO'"
    
} else {
    Write-Host "   No documents with files found" -ForegroundColor Yellow
    Write-Host "   Upload a document first!"
}

Write-Host "`n"

