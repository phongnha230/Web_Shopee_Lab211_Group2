# Fix orphaned products - Link to correct shops
# This script fixes products that are linked to non-existent "demo-shop-1"

$baseUrl = "http://localhost:8080/api"

# Known shops
$shops = @{
    "PaPaPee" = "6987fca7fe6fb0680294ed8a"
    "Arina"   = "697d92b28929569335e85fe5"  # Will verify if exists
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  FIX ORPHANED PRODUCTS SCRIPT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verify shops exist
Write-Host "Verifying shops..." -ForegroundColor Cyan
$validShops = @{}
foreach ($shopName in $shops.Keys) {
    $shopId = $shops[$shopName]
    try {
        $shop = Invoke-RestMethod -Uri "$baseUrl/shop/$shopId" -ErrorAction Stop
        Write-Host "  ✓ $shopName (ID: $shopId) - EXISTS" -ForegroundColor Green
        $validShops[$shopName] = $shopId
    }
    catch {
        Write-Host "  ✗ $shopName (ID: $shopId) - NOT FOUND" -ForegroundColor Red
    }
}

if ($validShops.Count -eq 0) {
    Write-Host "`nNo valid shops found. Cannot proceed." -ForegroundColor Red
    exit 1
}

# Fetch all products
Write-Host "`nFetching products..." -ForegroundColor Cyan
try {
    $products = Invoke-RestMethod -Uri "$baseUrl/products" -ErrorAction Stop
    Write-Host "  ✓ Found $($products.Count) total products" -ForegroundColor Green
}
catch {
    Write-Host "  ✗ Failed to fetch products" -ForegroundColor Red
    exit 1
}

# Find orphaned products (linked to demo-shop-1)
$orphanedProducts = $products | Where-Object { $_.shopId -eq "demo-shop-1" }
Write-Host "`nFound $($orphanedProducts.Count) orphaned products (linked to 'demo-shop-1')" -ForegroundColor Yellow

if ($orphanedProducts.Count -eq 0) {
    Write-Host "No orphaned products found. All good!" -ForegroundColor Green
    exit 0
}

# Show orphaned products
Write-Host "`nOrphaned products:" -ForegroundColor Cyan
foreach ($p in $orphanedProducts) {
    Write-Host "  - $($p.name) (ID: $($p.id))" -ForegroundColor White
}

# Ask which shop to assign them to
Write-Host "`nAvailable shops:" -ForegroundColor Cyan
$shopList = @($validShops.Keys)
for ($i = 0; $i -lt $shopList.Count; $i++) {
    Write-Host "  [$($i+1)] $($shopList[$i])" -ForegroundColor White
}

Write-Host ""
$choice = Read-Host "Which shop should these products be assigned to? (1-$($shopList.Count))"
$choiceIndex = [int]$choice - 1

if ($choiceIndex -lt 0 -or $choiceIndex -ge $shopList.Count) {
    Write-Host "Invalid choice. Exiting." -ForegroundColor Red
    exit 1
}

$targetShopName = $shopList[$choiceIndex]
$targetShopId = $validShops[$targetShopName]

Write-Host "`nWill assign all orphaned products to: $targetShopName" -ForegroundColor Yellow
$confirmation = Read-Host "Proceed? (yes/no)"

if ($confirmation -ne "yes") {
    Write-Host "Operation cancelled." -ForegroundColor Yellow
    exit 0
}

# Update products
Write-Host "`nUpdating products..." -ForegroundColor Cyan
$successCount = 0
$failCount = 0

foreach ($p in $orphanedProducts) {
    Write-Host "  Updating: $($p.name)..." -NoNewline
    
    $body = @{
        shopId = $targetShopId
    } | ConvertTo-Json
    
    try {
        Invoke-RestMethod -Uri "$baseUrl/products/$($p.id)" `
            -Method Put `
            -Body $body `
            -ContentType "application/json" `
            -ErrorAction Stop | Out-Null
        
        Write-Host " ✓" -ForegroundColor Green
        $successCount++
    }
    catch {
        Write-Host " ✗" -ForegroundColor Red
        Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
        $failCount++
    }
}

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Successfully updated: $successCount products" -ForegroundColor Green
Write-Host "Failed: $failCount products" -ForegroundColor Red
Write-Host ""
Write-Host "All orphaned products are now linked to '$targetShopName'!" -ForegroundColor Green
