# Safe script to fix shop links for products
# This script will UPDATE products to point to the correct shop
# NO PRODUCTS WILL BE DELETED - only shopId field will be updated

$baseUrl = "http://localhost:8080/api"
$correctShopId = "697dadea2c4624e9a1b1508a"  # Baribanai shop
$invalidShopIds = @("demo-shop-1")  # Shop IDs that don't exist

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SAFE PRODUCT SHOP LINK FIX SCRIPT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "This script will:" -ForegroundColor Yellow
Write-Host "  1. Find products linked to non-existent shops" -ForegroundColor Yellow
Write-Host "  2. Update them to point to 'Baribanai' shop" -ForegroundColor Yellow
Write-Host "  3. NO products will be deleted" -ForegroundColor Green
Write-Host ""

# Fetch all products
Write-Host "Fetching all products..." -ForegroundColor Cyan
try {
    $products = Invoke-RestMethod -Uri "$baseUrl/products" -Method Get -ErrorAction Stop
    Write-Host "✓ Found $($products.Count) total products" -ForegroundColor Green
}
catch {
    Write-Host "✗ Failed to fetch products. Is backend running?" -ForegroundColor Red
    exit 1
}

# Find products with invalid shopId
$productsToFix = $products | Where-Object { $invalidShopIds -contains $_.shopId }
Write-Host ""
Write-Host "Found $($productsToFix.Count) products with invalid shopId" -ForegroundColor Yellow

if ($productsToFix.Count -eq 0) {
    Write-Host "No products need fixing. All good!" -ForegroundColor Green
    exit 0
}

# Show products that will be updated
Write-Host ""
Write-Host "Products that will be updated:" -ForegroundColor Cyan
foreach ($p in $productsToFix) {
    Write-Host "  - $($p.name) (ID: $($p.id))" -ForegroundColor White
}

# Ask for confirmation
Write-Host ""
$confirmation = Read-Host "Do you want to proceed with the update? (yes/no)"
if ($confirmation -ne "yes") {
    Write-Host "Operation cancelled by user." -ForegroundColor Yellow
    exit 0
}

# Update products
Write-Host ""
Write-Host "Updating products..." -ForegroundColor Cyan
$successCount = 0
$failCount = 0

foreach ($p in $productsToFix) {
    Write-Host "Updating: $($p.name)..." -NoNewline
    
    $body = @{
        shopId = $correctShopId
    } | ConvertTo-Json
    
    try {
        Invoke-RestMethod -Uri "$baseUrl/products/$($p.id)" `
            -Method Put `
            -Body $body `
            -ContentType "application/json" `
            -ErrorAction Stop | Out-Null
        
        Write-Host " ✓ SUCCESS" -ForegroundColor Green
        $successCount++
    }
    catch {
        Write-Host " ✗ FAILED" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
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
Write-Host "All products are now linked to 'Baribanai' shop!" -ForegroundColor Green
