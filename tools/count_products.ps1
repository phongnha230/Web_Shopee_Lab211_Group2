$baseUrl = "http://localhost:8080/api"
$shop1Id = "69785193e54d760f00ecb62a"
$baribanaiId = "697dadea2c4624e9a1b1508a"

try {
    $p1 = Invoke-RestMethod -Uri "$baseUrl/products/shop/$shop1Id"
    $p2 = Invoke-RestMethod -Uri "$baseUrl/products/shop/$baribanaiId"
    
    $c1 = if ($p1) { $p1.Count } else { 0 }
    $cb = if ($p2) { $p2.Count } else { 0 }

    Write-Host "Shop 1: $c1 products"
    Write-Host "Baribanai: $cb products"
}
catch {
    Write-Host "Error: $($_.Exception.Message)"
}
