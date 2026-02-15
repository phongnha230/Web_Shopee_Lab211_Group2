# API Test Script - Product Debug
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "API DIAGNOSTIC TEST" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080/api"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TEST 1: Get ALL Products" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/products" -Method GET -ErrorAction Stop
    
    Write-Host "Success! Total Products Found: $($response.Count)" -ForegroundColor Green
    Write-Host ""
    
    if ($response.Count -gt 0) {
        Write-Host "Products Summary:" -ForegroundColor Cyan
        
        $shopGroups = $response | Group-Object -Property shopId
        
        foreach ($group in $shopGroups) {
            Write-Host "Shop ID: $($group.Name)" -ForegroundColor Yellow
            Write-Host "Products: $($group.Count)" -ForegroundColor White
            
            foreach ($product in $group.Group) {
                $shortId = if ($product.id.Length -gt 8) { $product.id.Substring(0, 8) + "..." } else { $product.id }
                Write-Host "  - $($product.name) (ID: $shortId)" -ForegroundColor Gray
            }
            Write-Host ""
        }
        
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "ANALYSIS:" -ForegroundColor Yellow
        Write-Host "Found $($response.Count) total products" -ForegroundColor White
        Write-Host "Distributed across $($shopGroups.Count) shop(s)" -ForegroundColor White
        Write-Host ""
        
        # Save shop IDs to file for reference
        $shopIds = $shopGroups | ForEach-Object { $_.Name }
        $shopIds | Out-File -FilePath "shop-ids.txt" -Encoding UTF8
        Write-Host "Shop IDs saved to: shop-ids.txt" -ForegroundColor Green
        Write-Host ""
        
    }
    else {
        Write-Host "No products found in database!" -ForegroundColor Red
        Write-Host "Products were never created successfully" -ForegroundColor Yellow
        Write-Host ""
    }
    
    # Save full response
    $response | ConvertTo-Json -Depth 10 | Out-File -FilePath "api-test-results.json" -Encoding UTF8
    Write-Host "Full API response saved to: api-test-results.json" -ForegroundColor Green
    
}
catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 401) {
        Write-Host "Endpoint requires authentication (401)" -ForegroundColor Yellow
    }
    else {
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TEST 2: Backend Health Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

try {
    $null = Invoke-WebRequest -Uri "http://localhost:8080" -Method GET -UseBasicParsing -TimeoutSec 2
    Write-Host "Backend is UP and responding!" -ForegroundColor Green
}
catch {
    Write-Host "Cannot connect to backend" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "NEXT STEPS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Open seller-dashboard.html in browser" -ForegroundColor White
Write-Host "2. Press F12 to open Console" -ForegroundColor White  
Write-Host "3. Look for Shop ID in console logs" -ForegroundColor White
Write-Host "4. Compare with Shop IDs shown above" -ForegroundColor White
Write-Host ""
