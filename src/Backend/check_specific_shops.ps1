$baseUrl = "http://localhost:8080/api"

Write-Output "Checking specific shops..."

try {
    $active = Invoke-RestMethod -Uri "$baseUrl/shop/admin/active" -Method Get
    
    foreach ($shop in $active) {
        if ($shop.name -match "Shop Công Nghệ Số 1" -or $shop.name -match "Baribanai") {
            $info = "FOUND: $($shop.name) | ID: $($shop.id) | OwnerEmail: $($shop.ownerEmail) | ContactEmail: $($shop.email)"
            Write-Output $info
        }
    }
}
catch {
    Write-Output "Error: $($_.Exception.Message)"
}
