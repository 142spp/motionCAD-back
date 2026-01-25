# MotionCAD Unified Development Script
# Starts Docker DB and runs Spring Boot locally

Write-Host "Starting Docker Database..." -ForegroundColor Cyan
docker compose up db -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start Docker DB. Please make sure Docker is running." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "Starting Spring Boot Application..." -ForegroundColor Cyan
./gradlew bootRun
