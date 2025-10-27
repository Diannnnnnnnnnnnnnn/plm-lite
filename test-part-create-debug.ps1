# Debug Part Creation
Write-Host "Debug: Creating Part..." -ForegroundColor Yellow

$baseUrl = "http://localhost:8089"

$partRequest = @{
    title = "Test Part"
    description = "Test description"
    stage = "IN_WORK"
    status = "DRAFT"
    level = "ASSEMBLY"
    creator = "test"
} | ConvertTo-Json

Write-Host "Request Body:" -ForegroundColor Cyan
Write-Host $partRequest
Write-Host ""

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/parts" -Method Post -Body $partRequest -ContentType "application/json" -UseBasicParsing
    Write-Host "Success!" -ForegroundColor Green
    Write-Host $response.Content
} catch {
    Write-Host "ERROR:" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "Status Description: $($_.Exception.Response.StatusDescription)" -ForegroundColor Red
    
    # Get the actual error message from response
    $result = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($result)
    $responseBody = $reader.ReadToEnd()
    
    Write-Host ""
    Write-Host "Response Body:" -ForegroundColor Yellow
    Write-Host $responseBody
}

