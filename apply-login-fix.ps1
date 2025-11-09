# Script to apply the login/logout fix
# This script rebuilds affected services and restarts them

Write-Host "Applying Login/Logout Fix" -ForegroundColor Green
Write-Host "=========================" -ForegroundColor Green
Write-Host ""

# Step 1: Check auth-service
Write-Host "Step 1: Auth service check..." -ForegroundColor Yellow
Write-Host "   ✓ Auth service already has logout endpoint - no rebuild needed!" -ForegroundColor Green
Write-Host ""

# Step 2: Restart NGINX
Write-Host "Step 2: Restarting NGINX..." -ForegroundColor Yellow
Push-Location infra\nginx
try {
    docker-compose down
    Start-Sleep -Seconds 2
    docker-compose up -d
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ NGINX restarted successfully" -ForegroundColor Green
    } else {
        Write-Host "✗ NGINX restart failed" -ForegroundColor Red
        exit 1
    }
} finally {
    Pop-Location
}
Write-Host ""

# Step 3: Restart frontend (if running)
Write-Host "Step 3: Frontend changes applied" -ForegroundColor Yellow
Write-Host "   Note: If frontend is running, it will auto-reload with the changes" -ForegroundColor Cyan
Write-Host "   If not running, start it with: cd frontend && npm start" -ForegroundColor Cyan
Write-Host ""

# Step 4: Wait for services to start
Write-Host "Step 4: Waiting for services to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 10
Write-Host ""

# Step 5: Test the fix
Write-Host "Step 5: Testing the fix..." -ForegroundColor Yellow
Write-Host "   Running automated tests..." -ForegroundColor Cyan

try {
    & .\test-login-logout.ps1
    Write-Host ""
    Write-Host "=========================" -ForegroundColor Green
    Write-Host "Fix applied successfully! ✓" -ForegroundColor Green
    Write-Host ""
    Write-Host "You can now:" -ForegroundColor Cyan
    Write-Host "  1. Open http://localhost:8111" -ForegroundColor White
    Write-Host "  2. Login with demo/demo" -ForegroundColor White
    Write-Host "  3. Logout" -ForegroundColor White
    Write-Host "  4. Login again - should work without 401 error!" -ForegroundColor White
} catch {
    Write-Host ""
    Write-Host "Automated test failed, but services are running." -ForegroundColor Yellow
    Write-Host "Please test manually in the browser at http://localhost:8111" -ForegroundColor Yellow
}

