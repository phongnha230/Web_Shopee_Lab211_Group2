$port = 8080
$process = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -First 1

if ($process) {
    Write-Host "⚠️ Found process $($process.OwningProcess) using port $port. Killing it..." -ForegroundColor Yellow
    Stop-Process -Id $process.OwningProcess -Force
    Start-Sleep -Seconds 1
} else {
    Write-Host "✅ Port $port is free." -ForegroundColor Green
}

Write-Host "🚀 Starting Backend..." -ForegroundColor Cyan
./mvnw spring-boot:run
