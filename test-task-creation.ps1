# Test Task Creation for Document 1e088e82-e7ca-4553-8481-43938f455fdb
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Testing Task Creation" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$documentId = "1e088e82-e7ca-4553-8481-43938f455fdb"
$taskServiceUrl = "http://localhost:8082/api/tasks"

# Test 1: Check if Task Service is running
Write-Host "[1] Checking Task Service health..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8082/actuator/health" -Method Get
    Write-Host "   Status: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "   ERROR: Task Service is not responding!" -ForegroundColor Red
    Write-Host "   Make sure the Task Service (Port 8082) is running." -ForegroundColor Red
    exit 1
}

# Test 2: Get all existing tasks
Write-Host "`n[2] Fetching all existing tasks..." -ForegroundColor Yellow
try {
    $tasks = Invoke-RestMethod -Uri $taskServiceUrl -Method Get
    Write-Host "   Total tasks: $($tasks.Count)" -ForegroundColor Green
    
    # Check if there are any tasks for this document
    $docTasks = $tasks | Where-Object { $_.contextId -eq $documentId -or $_.taskDescription -like "*$documentId*" }
    if ($docTasks) {
        Write-Host "   Found existing tasks for document:" -ForegroundColor Cyan
        foreach ($task in $docTasks) {
            Write-Host "     - Task ID: $($task.id) | Status: $($task.status) | Assigned To: $($task.assignedTo)" -ForegroundColor White
        }
    } else {
        Write-Host "   No existing tasks found for this document." -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ERROR: Failed to fetch tasks: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Create a manual test task
Write-Host "`n[3] Creating manual review task for document..." -ForegroundColor Yellow
$taskBody = @{
    taskName = "Manual Review: Document 1108001"
    taskDescription = "Please review document 1108001 (ID: $documentId)`n`nThis is a manually created test task."
    taskType = "REVIEW"
    assignedTo = "demo"
    assignedBy = "system"
    contextType = "DOCUMENT"
    contextId = $documentId
    priority = 1
} | ConvertTo-Json

try {
    $newTask = Invoke-RestMethod -Uri $taskServiceUrl -Method Post -Body $taskBody -ContentType "application/json"
    Write-Host "   SUCCESS: Task created!" -ForegroundColor Green
    Write-Host "   Task ID: $($newTask.id)" -ForegroundColor White
    Write-Host "   Task Name: $($newTask.taskName)" -ForegroundColor White
    Write-Host "   Assigned To: $($newTask.assignedTo)" -ForegroundColor White
    Write-Host "`n   The reviewer 'demo' should now see this task in the UI!" -ForegroundColor Cyan
} catch {
    Write-Host "   ERROR: Failed to create task: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.BaseStream.Position = 0
        $reader.DiscardBufferedData()
        $responseBody = $reader.ReadToEnd()
        Write-Host "   Response: $responseBody" -ForegroundColor Red
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Test Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan





