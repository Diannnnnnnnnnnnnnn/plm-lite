# Test: Submit a document for two-stage review
Write-Host ""
Write-Host "========================================"
Write-Host "Testing Two-Stage Review Workflow"
Write-Host "========================================"
Write-Host ""

# Step 1: Start the workflow with two reviewers
Write-Host "Step 1: Starting workflow with initial reviewer (vivi) and technical reviewer (labubu)..."

$startPayload = @{
    documentId = "DOC-001"
    masterId = "MASTER-001"
    version = "1.0"
    creator = "alice"
    initialReviewer = "vivi"
    technicalReviewer = "labubu"
} | ConvertTo-Json

Write-Host "Payload: $startPayload"

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8086/api/workflows/document-approval/start" -Method POST -ContentType "application/json" -Body $startPayload -UseBasicParsing
    Write-Host "SUCCESS: Workflow started!"
    Write-Host "Response: $($response.Content)"
} catch {
    Write-Host "ERROR: Failed to start workflow"
    Write-Host "Error: $_"
    exit 1
}

# Wait a bit for the workflow to process
Write-Host ""
Write-Host "Waiting 3 seconds for workflow to process..."
Start-Sleep -Seconds 3

# Step 2: Check vivi's tasks
Write-Host ""
Write-Host "Step 2: Checking tasks for vivi..."

try {
    $tasks = Invoke-WebRequest -Uri "http://localhost:8082/tasks?assignedTo=vivi" -Method GET -UseBasicParsing
    $taskList = $tasks.Content | ConvertFrom-Json
    
    if ($taskList.Count -gt 0) {
        Write-Host "SUCCESS: Found $($taskList.Count) task(s) for vivi:"
        foreach ($task in $taskList) {
            Write-Host "  - Task ID: $($task.id)"
            Write-Host "    Name: $($task.name)"
            Write-Host "    Status: $($task.status)"
            Write-Host "    ReviewStage: $($task.reviewStage)"
            Write-Host "    InitialReviewer: $($task.initialReviewer)"
            Write-Host "    TechnicalReviewer: $($task.technicalReviewer)"
        }
    } else {
        Write-Host "ERROR: No tasks found for vivi!"
        Write-Host "This means the initial review task was not created."
    }
} catch {
    Write-Host "ERROR: Failed to get tasks"
    Write-Host "Error: $_"
}

Write-Host ""
Write-Host "========================================"
Write-Host "NOW CHECK THE WORKFLOW-ORCHESTRATOR WINDOW"
Write-Host "Look for these log messages:"
Write-Host "  - Starting document approval workflow"
Write-Host "  - Two-Stage Review Mode:"
Write-Host "  - Stage 1: Waiting for INITIAL REVIEW"
Write-Host "  - Created initial review task ID: X"
Write-Host "Or any ERROR messages"
Write-Host "========================================"
Write-Host ""
