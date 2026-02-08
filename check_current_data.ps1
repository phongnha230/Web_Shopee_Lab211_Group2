$baseUrl = "http://localhost:8080/api"

function Get-ProductsForShop {
    param ($shopId, $shopName)
    try {
        $products = Invoke-RestMethod -Uri "$baseUrl/products/shop/$shopId" -Method Get -ErrorAction Stop
        $count = if ($products) { $products.Count } else { 0 }
        Write-Host "  - Shop: $shopName (ID: $shopId)"
        Write-Host "    Total Products: $count"
        if ($count -gt 0) {
            foreach ($p in $products) {
                Write-Host "      * [$($p.id)] $($p.name) - Price: $($p.originalPrice)"
            }
        }
    }
    catch {
        Write-Host "    Error fetching products for shop $shopName ($shopId): $($_.Exception.Message)"
    }
}

Write-Host "`n=== CHECKING SHOPS AND PRODUCTS ===`n"

# 1. Active Shops
Write-Host "--- ACTIVE SHOPS ---"
try {
    $activeShops = Invoke-RestMethod -Uri "$baseUrl/shop/admin/active" -Method Get -ErrorAction Stop
    if ($activeShops.Count -eq 0) {
        Write-Host "No active shops found."
    }
    foreach ($shop in $activeShops) {
        Get-ProductsForShop -shopId $shop.id -shopName $shop.name
    }
} catch {
    Write-Host "Error fetching active shops: $($_.Exception.Message)"
}

# 2. Pending Shops
Write-Host "`n--- PENDING SHOPS ---"
try {
    $pendingShops = Invoke-RestMethod -Uri "$baseUrl/shop/admin/pending" -Method Get -ErrorAction Stop
    if ($pendingShops.Count -eq 0) {
        Write-Host "No pending shops found."
    }
    foreach ($shop in $pendingShops) {
        Get-ProductsForShop -shopId $shop.id -shopName $shop.name
    }
} catch {
    Write-Host "Error fetching pending shops: $($_.Exception.Message)"
}

# 3. Rejected Shops
Write-Host "`n--- REJECTED SHOPS ---"
try {
    $rejectedShops = Invoke-RestMethod -Uri "$baseUrl/shop/admin/rejected" -Method Get -ErrorAction Stop
    if ($rejectedShops.Count -eq 0) {
        Write-Host "No rejected shops found."
    }
    foreach ($shop in $rejectedShops) {
        Get-ProductsForShop -shopId $shop.id -shopName $shop.name
    }
} catch {
    Write-Host "Error fetching rejected shops: $($_.Exception.Message)"
}

Write-Host "`n=== DONE ===`n"
