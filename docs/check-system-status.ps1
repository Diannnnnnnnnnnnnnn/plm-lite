# =====================================================
# PLM Lite - System Status Checker
# Check all services and their health
# =====================================================

$ErrorActionPreference = "Continue"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  PLM Lite - System Status Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Function to test HTTP endpoint
function Test-HttpEndpoint {
    param([string]$Url, [int]$TimeoutSec = 2)
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec $TimeoutSec -ErrorAction SilentlyContinue
        return @{Status=$true; StatusCode=$response.StatusCode}
    } catch {
        return @{Status=$false; StatusCode=0}
    }
}

# Function to test TCP port
function Test-Port {
    param([string]$HostName, [int]$Port)
    $connection = Test-NetConnection -ComputerName $HostName -Port $Port -InformationLevel Quiet -WarningAction SilentlyContinue
    return $connection
}

# Function to display status
function Show-ServiceStatus {
    param(
        [string]$Name,
        [bool]$IsRunning,
        [string]$Url = "",
        [int]$StatusCode = 0
    )
    
    $status = if ($IsRunning) {
        Write-Host ("  [OK] {0,-25}" -f $Name) -ForegroundColor Green -NoNewline
        if ($StatusCode -gt 0) {
            Write-Host (" [HTTP {0}]" -f $StatusCode) -ForegroundColor Gray -NoNewline
        }
        Write-Host " - RUNNING" -ForegroundColor Green
    } else {
        Write-Host ("  [--] {0,-25}" -f $Name) -ForegroundColor Red -NoNewline
        Write-Host " - NOT RUNNING" -ForegroundColor Red
    }
    
    if ($Url -ne "") {
        Write-Host ("    {0}" -f $Url) -ForegroundColor Gray
    }
}

# Check Docker
Write-Host "Docker Status:" -ForegroundColor Yellow
try {
    docker ps | Out-Null
    Write-Host "  [OK] Docker Engine           - RUNNING" -ForegroundColor Green
} catch {
    Write-Host "  [--] Docker Engine           - NOT RUNNING" -ForegroundColor Red
}
Write-Host ""

# Check Docker Containers
Write-Host "Docker Containers:" -ForegroundColor Yellow
$containers = @(
    @{Name="Elasticsearch"; Container="plm-elasticsearch"; Url="http://localhost:9200"},
    @{Name="Kibana"; Container="plm-kibana"; Url="http://localhost:5601"},
    @{Name="Zeebe"; Container="zeebe"; Url="http://localhost:8088"},
    @{Name="Operate"; Container="operate"; Url="http://localhost:8181"},
    @{Name="Tasklist"; Container="tasklist"; Url="http://localhost:8182"},
    @{Name="Connectors"; Container="connectors"; Url="http://localhost:8085"},
    @{Name="MinIO"; Container="minio"; Url="http://localhost:9001"},
    @{Name="Redis"; Container="redis"; Url=""},
    @{Name="MySQL"; Container="mysql-plm"; Url=""},
    @{Name="Neo4j"; Container="neo4j-plm"; Url="http://localhost:7474"}
)

foreach ($container in $containers) {
    $running = docker ps --format "{{.Names}}" | Select-String -Pattern "^$($container.Container)$" -Quiet
    
    if ($running) {
        # Check health status
        $health = docker inspect --format='{{.State.Health.Status}}' $container.Container 2>$null
        $state = docker inspect --format='{{.State.Status}}' $container.Container 2>$null
        
        $statusText = if ($health -eq "healthy") {
            "HEALTHY"
        } elseif ($health -eq "unhealthy") {
            "UNHEALTHY"
        } elseif ($state -eq "running") {
            "RUNNING"
        } else {
            $state.ToUpper()
        }
        
        $color = if ($health -eq "healthy" -or ($state -eq "running" -and $health -eq "")) {
            "Green"
        } elseif ($health -eq "unhealthy") {
            "Red"
        } else {
            "Yellow"
        }
        
        Write-Host ("  [OK] {0,-25}" -f $container.Name) -ForegroundColor $color -NoNewline
        Write-Host (" - {0}" -f $statusText) -ForegroundColor $color
    } else {
        Write-Host ("  [--] {0,-25}" -f $container.Name) -ForegroundColor Red -NoNewline
        Write-Host " - NOT RUNNING" -ForegroundColor Red
    }
    
    if ($container.Url -ne "") {
        Write-Host ("    {0}" -f $container.Url) -ForegroundColor Gray
    }
}
Write-Host ""

# Check Backend Services
Write-Host "Backend Services:" -ForegroundColor Yellow
$backendServices = @(
    @{Name="Graph Service"; Port=8090; Endpoint="/actuator/health"},
    @{Name="Workflow Orchestrator"; Port=8086; Endpoint="/actuator/health"},
    @{Name="User Service"; Port=8083; Endpoint="/actuator/health"},
    @{Name="Task Service"; Port=8082; Endpoint="/actuator/health"},
    @{Name="Document Service"; Port=8081; Endpoint="/actuator/health"},
    @{Name="BOM Service"; Port=8089; Endpoint="/actuator/health"},
    @{Name="Change Service"; Port=8084; Endpoint="/actuator/health"},
    @{Name="Search Service"; Port=8091; Endpoint="/actuator/health"}
)

foreach ($service in $backendServices) {
    $url = "http://localhost:$($service.Port)$($service.Endpoint)"
    $result = Test-HttpEndpoint $url
    Show-ServiceStatus -Name $service.Name -IsRunning $result.Status -Url "http://localhost:$($service.Port)" -StatusCode $result.StatusCode
}
Write-Host ""

# Check Frontend
Write-Host "Frontend:" -ForegroundColor Yellow
$frontendRunning = Test-Port "localhost" 3000
Show-ServiceStatus -Name "React Application" -IsRunning $frontendRunning -Url "http://localhost:3000"
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$totalContainers = $containers.Count
$runningContainers = 0
foreach ($container in $containers) {
    $running = docker ps --format "{{.Names}}" | Select-String -Pattern "^$($container.Container)$" -Quiet
    if ($running) { $runningContainers++ }
}

$totalServices = $backendServices.Count + 1  # +1 for frontend
$runningServices = 0
foreach ($service in $backendServices) {
    if (Test-Port "localhost" $service.Port) { $runningServices++ }
}
if (Test-Port "localhost" 3000) { $runningServices++ }

Write-Host ("  Containers:  {0}/{1} running" -f $runningContainers, $totalContainers)
Write-Host ("  Services:    {0}/{1} running" -f $runningServices, $totalServices)
Write-Host ""

if ($runningContainers -eq $totalContainers -and $runningServices -eq $totalServices) {
    Write-Host "  System Status: FULLY OPERATIONAL" -ForegroundColor Green
} elseif ($runningContainers -gt 0 -or $runningServices -gt 0) {
    Write-Host "  System Status: PARTIALLY RUNNING" -ForegroundColor Yellow
} else {
    Write-Host "  System Status: NOT RUNNING" -ForegroundColor Red
}
Write-Host ""

# Quick Actions
Write-Host "Quick Actions:" -ForegroundColor Yellow
Write-Host "  Start system:    .\start-plm-system.ps1"
Write-Host "  Stop services:   .\stop-all-services.ps1"
Write-Host "  Stop containers: docker-compose -f docker-compose-master.yml down"
Write-Host "  View logs:       docker-compose -f docker-compose-master.yml logs -f"
Write-Host ""

