$baseUrl = "http://localhost:8080/api"

try {
    $products = Invoke-RestMethod -Uri "$baseUrl/products" -Method Get -ErrorAction Stop
    
    Write-Host "Found $($products.Count) products in total." -ForegroundColor Cyan
    Write-Host "--------------------------------------------------------------------------------"
    Write-Host ("{0,-40} | {1,-25} | {2,-30}" -f "Product Name", "Product ID", "Shop ID")
    Write-Host "--------------------------------------------------------------------------------"
    
    foreach ($p in $products) {
        $shopId = "NULL"
        if ($p.shopId) { $shopId = $p.shopId }
        elseif ($p.shop -and $p.shop.id) { $shopId = $p.shop.id }
        
        $name = $p.name
        if ($name.Length -gt 38) { $name = $name.Substring(0, 35) + "..." }

        Write-Host ("{0,-40} | {1,-25} | {2,-30}" -f $name, $p.id, $shopId)
    }
    Write-Host "--------------------------------------------------------------------------------"

}
catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}
