# ============================================
# Stop All PLM Services (Backend + Frontend)
# ============================================

Write-Host "`nStopping all PLM services..." -ForegroundColor Yellow

# Stop all Java processes (Backend services)
Write-Host "`nStopping backend services (Java)..." -ForegroundColor White
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue

if ($javaProcesses) {
    Write-Host "  Found $($javaProcesses.Count) Java process(es)" -ForegroundColor Gray
    $javaProcesses | Stop-Process -Force
    Write-Host "  ✓ Backend services stopped" -ForegroundColor Green
} else {
    Write-Host "  No Java services were running" -ForegroundColor Gray
}

# Stop all Node.js processes (Frontend)
Write-Host "`nStopping frontend (Node.js)..." -ForegroundColor White
$nodeProcesses = Get-Process -Name "node" -ErrorAction SilentlyContinue

if ($nodeProcesses) {
    Write-Host "  Found $($nodeProcesses.Count) Node.js process(es)" -ForegroundColor Gray
    $nodeProcesses | Stop-Process -Force
    Write-Host "  ✓ Frontend stopped" -ForegroundColor Green
} else {
    Write-Host "  No Node.js services were running" -ForegroundColor Gray
}

Start-Sleep -Seconds 2

# Verify all stopped
$remainingJava = Get-Process -Name "java" -ErrorAction SilentlyContinue
$remainingNode = Get-Process -Name "node" -ErrorAction SilentlyContinue

if ($remainingJava -or $remainingNode) {
    Write-Host "`n⚠️  Warning: Some processes still running" -ForegroundColor Red
    if ($remainingJava) { Write-Host "  Java: $($remainingJava.Count)" -ForegroundColor Red }
    if ($remainingNode) { Write-Host "  Node: $($remainingNode.Count)" -ForegroundColor Red }
} else {
    Write-Host "`n✓ All services stopped successfully`n" -ForegroundColor Green
}
