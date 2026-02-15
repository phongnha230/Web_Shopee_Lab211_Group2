$baseUrl = "http://localhost:8080/api"

Write-Output "Fetching all shops (Active, Pending, Rejected)..."

try {
    $active = Invoke-RestMethod -Uri "$baseUrl/shop/admin/active" -Method Get
    $pending = Invoke-RestMethod -Uri "$baseUrl/shop/admin/pending" -Method Get
    $rejected = Invoke-RestMethod -Uri "$baseUrl/shop/admin/rejected" -Method Get
    
    $allShops = $active + $pending + $rejected
    
    Write-Output "Found $($allShops.Count) total shops."
    
    foreach ($shop in $allShops) {
        Write-Output "------------------------------------------------"
        Write-Output "Name: $($shop.name)"
        Write-Output "ID: $($shop.id)"
        Write-Output "Owner/Email: $($shop.email)"
    }
    
}
catch {
    Write-Output "Error fetching shops: $($_.Exception.Message)"
}
