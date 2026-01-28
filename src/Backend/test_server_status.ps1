# Quick test upload - Simple version
$TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJwaG9uZ25oYTIzMEBnbWFpbC5jb20iLCJpYXQiOjE3Mzc5MjA5MzUsImV4cCI6MTczNzkyNDUzNX0.KmL3jHXuVMJRN9UkfWlbhTp5QT3-AxSKLKlnxaVZmLQNJjPM_4hgvGqJKQ93P-7h1rxTy8dYk9gKHFtjBXzpgw"

Write-Host "`n=== Quick Test: Check Server Status ===" -ForegroundColor Cyan

# Test if server is running
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/shop/my-shop" `
        -Method GET `
        -Headers @{ "Authorization" = "Bearer $TOKEN" } `
        -ErrorAction Stop

    Write-Host "✅ Server is running!" -ForegroundColor Green
    Write-Host "Response: $($response | ConvertTo-Json -Depth 2)" -ForegroundColor Yellow
    
}
catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "Server responded with status: $statusCode" -ForegroundColor Yellow
    
    if ($statusCode -eq 401) {
        Write-Host "❌ Token expired - need to login again" -ForegroundColor Red
    }
    elseif ($statusCode -eq 404) {
        Write-Host "✅ Server is running (endpoint returned 404 - normal if no shop exists)" -ForegroundColor Green
    }
    else {
        Write-Host "Response: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== Server Status Check Complete ===" -ForegroundColor Cyan
