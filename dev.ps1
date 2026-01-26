# MotionCAD Unified Development Script
# Starts Docker DB and runs Spring Boot locally

Write-Host "Starting Docker Database..." -ForegroundColor Cyan
docker compose up db -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start Docker DB. Please make sure Docker is running." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "Starting Spring Boot Application..." -ForegroundColor Cyan

# Load .env file into environment variables
if (Test-Path ".env") {
    Write-Host "Loading .env file..." -ForegroundColor DarkGray
    Get-Content .env | ForEach-Object {
        $line = $_.Trim()
        if ($line -match '^[^#\s=]+=' ) {
            $key, $value = $line.Split('=', 2)
            $key = $key.Trim()
            $value = $value.Trim()
            [System.Environment]::SetEnvironmentVariable($key, $value, "Process")
        }
    }
}

./gradlew bootRun
