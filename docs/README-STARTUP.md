# PLM-Lite Startup Guide

This guide explains how to start and stop all PLM-Lite services using the provided scripts.

## Quick Start

### Windows Batch Script (Recommended for simplicity)
Simply double-click `start-all-services.bat` to start all services.

### PowerShell Script (More features)
Right-click `start-all-services.ps1` and select "Run with PowerShell"

## Services Started

When you run the startup script, the following services will be started:

### Backend Services
- **Document Service** - Port 8081
- **Task Service** - Port 8082
- **User Service** - Port 8083
- **Change Service** - Port 8084
- **BOM Service** - Port 8089

### Frontend
- **React Application** - Port 3001

## Access the Application

Once all services are running (wait 1-2 minutes), open your browser and navigate to:
```
http://localhost:3001
```

## Stopping Services

### Using Batch Script
Double-click `stop-all-services.bat`

### Using PowerShell
Right-click `stop-all-services.ps1` and select "Run with PowerShell"

### Manual Stop
If scripts don't work, you can manually stop services by closing all the terminal windows that were opened, or run:
```powershell
Stop-Process -Name java -Force
Stop-Process -Name node -Force
```

## Troubleshooting

### Port Already in Use
If you get "Port already in use" errors:
1. Run the stop script first: `stop-all-services.bat`
2. Wait 5 seconds
3. Run the start script again: `start-all-services.bat`

### Services Not Starting
1. Make sure you have Java 17 and Node.js installed
2. Check that Maven is configured correctly
3. Verify all dependencies are installed with `mvn clean install` in the root directory

### Frontend Not Accessible
If the frontend doesn't load:
1. Check if Node is running: `Get-Process -Name node`
2. Verify port 3001 is listening: `netstat -ano | findstr :3001`
3. Check the frontend console window for errors

## Service Startup Order

The scripts start services in this order:
1. BOM Service (8089)
2. Change Service (8084)
3. Document Service (8081)
4. Task Service (8082)
5. User Service (8083)
6. Frontend (3001)

There's a 60-second wait between backend services and frontend to ensure backend services are ready.

## Notes

- Each service runs in its own terminal window
- Keep these windows open while using the application
- Closing a window will stop that specific service
- Services may take 1-2 minutes to fully initialize
- Look for "Started [ServiceName]Application" message in each window to confirm successful startup
