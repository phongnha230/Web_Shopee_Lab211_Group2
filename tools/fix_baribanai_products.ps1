$baseUrl = "http://localhost:8080/api"
$baribanaiId = "697dadea2c4624e9a1b1508a"

# List of Shop IDs to move products FROM
$targetShopIds = @("demo-shop-1", "69785193e54d760f00ecb62a")

Write-Host "Fetching all products..." -ForegroundColor Cyan
try {
    $products = Invoke-RestMethod -Uri "$baseUrl/products" -Method Get -ErrorAction Stop
}
catch {
    Write-Host "Failed to fetch products. Is backend running?" -ForegroundColor Red
    exit
}

$count = 0
foreach ($p in $products) {
    $currentShopId = ""
    if ($p.shopId) { $currentShopId = $p.shopId }
    elseif ($p.shop -and $p.shop.id) { $currentShopId = $p.shop.id }

    if ($targetShopIds -contains $currentShopId) {
        Write-Host "Moving product: $($p.name) (ID: $($p.id)) from $currentShopId to Baribanai..." -NoNewline
        
        $body = @{
            shopId = $baribanaiId
        } | ConvertTo-Json

        try {
            Invoke-RestMethod -Uri "$baseUrl/products/$($p.id)" -Method Put -Body $body -ContentType "application/json" -ErrorAction Stop
            Write-Host " [OK]" -ForegroundColor Green
            $count++
        }
        catch {
            Write-Host " [FAILED]" -ForegroundColor Red
            Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
            # Print response body if available
            if ($_.ErrorDetails) { Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Red }
        }
    }
}

Write-Host "------------------------------------------------"
Write-Host "Successfully moved $count products to Baribanai." -ForegroundColor Cyan
