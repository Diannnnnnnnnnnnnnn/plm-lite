# Test with minimal fields (old way)
Write-Host "Test 1: Creating part WITHOUT new fields..." -ForegroundColor Yellow

$minimalPart = @{
    title = "Minimal Test Part"
    stage = "DETAILED_DESIGN"
    level = "PART"
    creator = "test"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8089/parts" -Method Post -Body $minimalPart -ContentType "application/json"
    Write-Host "SUCCESS! Part created: $($response.id)" -ForegroundColor Green
    Write-Host "Title: $($response.title)" -ForegroundColor Gray
    Write-Host "Status: $($response.status)" -ForegroundColor Gray
    Write-Host "Description: $($response.description)" -ForegroundColor Gray
} catch {
    Write-Host "FAILED with minimal fields" -ForegroundColor Red
    Write-Host $_.Exception.Message
}

Write-Host ""
Write-Host "Test 2: Creating part WITH description only..." -ForegroundColor Yellow

$withDescription = @{
    title = "Part With Description"
    description = "This has a description"
    stage = "DETAILED_DESIGN"
    level = "PART"
    creator = "test"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8089/parts" -Method Post -Body $withDescription -ContentType "application/json"
    Write-Host "SUCCESS! Part created: $($response.id)" -ForegroundColor Green
    Write-Host "Description: $($response.description)" -ForegroundColor Gray
} catch {
    Write-Host "FAILED with description" -ForegroundColor Red
    Write-Host $_.Exception.Message
}

Write-Host ""
Write-Host "Test 3: Creating part WITH status only..." -ForegroundColor Yellow

$withStatus = @{
    title = "Part With Status"
    stage = "DETAILED_DESIGN"
    status = "IN_WORK"
    level = "PART"
    creator = "test"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8089/parts" -Method Post -Body $withStatus -ContentType "application/json"
    Write-Host "SUCCESS! Part created: $($response.id)" -ForegroundColor Green
    Write-Host "Status: $($response.status)" -ForegroundColor Gray
} catch {
    Write-Host "FAILED with status" -ForegroundColor Red
    Write-Host $_.Exception.Message
}

