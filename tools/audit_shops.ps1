$baseUrl = "http://localhost:8080/api"

function Get-Data {
    param ($endpoint)
    try {
        return Invoke-RestMethod -Uri "$baseUrl$endpoint" -Method Get -ErrorAction Stop
    }
    catch {
        Write-Host "Error fetching $endpoint : $($_.Exception.Message)" -ForegroundColor Red
        return @()
    }
}

Write-Host "Fetching Shops..." -ForegroundColor Cyan
$activeShops = Get-Data "/shop/admin/active"
$pendingShops = Get-Data "/shop/admin/pending"
$rejectedShops = Get-Data "/shop/admin/rejected"

Write-Host "Fetching Products..." -ForegroundColor Cyan
$products = Get-Data "/products"

# Combine shops, ensuring no duplicates (by ID)
$allShops = @{}
if ($activeShops) { $activeShops | ForEach-Object { $allShops[$_.id] = $_ } }
if ($pendingShops) { $pendingShops | ForEach-Object { $allShops[$_.id] = $_ } }
if ($rejectedShops) { $rejectedShops | ForEach-Object { $allShops[$_.id] = $_ } }

Write-Host "`n=== SHOP AUDIT REPORT ===" -ForegroundColor Green
Write-Host "Found $($allShops.Count) unique shops." -ForegroundColor Gray

foreach ($shopId in $allShops.Keys) {
    $shop = $allShops[$shopId]
    $shopName = $shop.name
    $shopStatus = $shop.status # Use existing status property
    
    # Filter products for this shop
    $shopProducts = $products | Where-Object { $_.shopId -eq $shopId -or ($_.shop -and $_.shop.id -eq $shopId) }
    
    Write-Host "------------------------------------------------"
    Write-Host "SHOP: $shopName (ID: $shopId)" -ForegroundColor White
    
    if ($shopStatus -eq "ACTIVE") {
        Write-Host "STATUS: ACTIVE" -ForegroundColor Green
    }
    elseif ($shopStatus -eq "PENDING") {
        Write-Host "STATUS: PENDING" -ForegroundColor Yellow
    }
    else {
        Write-Host "STATUS: $shopStatus" -ForegroundColor Red
    }

    $productCount = @($shopProducts).Count
    Write-Host "PRODUCTS: $productCount"

    if ($productCount -gt 0) {
        foreach ($p in $shopProducts) {
            Write-Host "  - $($p.name) (Stock: $($p.stock), Price: $($p.price))" -ForegroundColor Gray
        }
    }
    else {
        Write-Host "  (No products)" -ForegroundColor DarkGray
    }
}

Write-Host "`n==========================" -ForegroundColor Green
