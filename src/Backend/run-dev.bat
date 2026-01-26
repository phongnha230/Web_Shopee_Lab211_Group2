@echo off
set PORT=8080

echo 🔍 Checking port %PORT%...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :%PORT% ^| findstr LISTENING') do (
    echo ⚠️ Found process %%a using port %PORT%. Killing it...
    taskkill /F /PID %%a
)

echo 🚀 Starting Backend...
mvnw spring-boot:run
