$baseUrl = "http://localhost:8080/api"
$shop1Id = "69785193e54d760f00ecb62a" 
$baribanaiId = "697dadea2c4624e9a1b1508a"

function Get-ShopInfo ($id) {
    try {
        $shop = Invoke-RestMethod -Uri "$baseUrl/shop/$id" -Method Get
        # Note: /api/shop/{id} returns Shop entity, not ShopAdminResponse.
        # Shop entity might not have ownerEmail field directly if it's not projected.
        # Let's check Shop.java to see what fields it has.
        # But wait, ShopController.java line 92 returns Shop.
        Write-Output "Shop: $($shop.name)"
        Write-Output "OwnerId: $($shop.ownerId)"
        Write-Output "Email: $($shop.email)"
        Write-Output "--------------------------------"
    }
    catch {
        Write-Output "Error fetching $id : $($_.Exception.Message)"
    }
}

Get-ShopInfo $shop1Id
Get-ShopInfo $baribanaiId
