$baseUrl = "http://localhost:8080/api"
$baribanaiId = "697dadea2c4624e9a1b1508a"
$secret = "YourSuperSecretKeyForJWTTokenGenerationAndValidation123456" # From .env

# 1. Get a valid email
try {
    $shops = Invoke-RestMethod -Uri "$baseUrl/shop/admin/active" -Method Get -ErrorAction Stop
    if ($shops.Count -eq 0) {
        Write-Host "No active shops found. Need a valid user email." -ForegroundColor Red
        exit
    }
    $validEmail = $shops[0].email
    Write-Host "Using email for token: $validEmail" -ForegroundColor Cyan
}
catch {
    Write-Host "Failed to fetch shops: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# 2. Function to generate JWT
function New-JwtToken {
    param (
        [string]$Email,
        [string]$SecretKey
    )

    $header = @{ alg = "HS256"; typ = "JWT" }
    $now = [Math]::Floor([decimal](Get-Date).ToUniversalTime().Subtract([datetime]"1970-01-01").TotalSeconds)
    $exp = $now + 3600 # 1 hour
    
    $payload = @{
        sub = $Email
        iat = $now
        exp = $exp
    }

    $headerJson = $header | ConvertTo-Json -Compress
    $payloadJson = $payload | ConvertTo-Json -Compress

    $Base64UrlEncode = {
        param($Bytes)
        [Convert]::ToBase64String($Bytes).Replace('+', '-').Replace('/', '_').TrimEnd('=')
    }

    $headerEncoded = &$Base64UrlEncode ([System.Text.Encoding]::UTF8.GetBytes($headerJson))
    $payloadEncoded = &$Base64UrlEncode ([System.Text.Encoding]::UTF8.GetBytes($payloadJson))

    $signatureInput = "$headerEncoded.$payloadEncoded"
    $hmac = New-Object System.Security.Cryptography.HMACSHA256
    $hmac.Key = [System.Text.Encoding]::UTF8.GetBytes($SecretKey)
    $signatureBytes = $hmac.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($signatureInput))
    $signatureEncoded = &$Base64UrlEncode $signatureBytes

    return "$headerEncoded.$payloadEncoded.$signatureEncoded"
}

$token = New-JwtToken -Email $validEmail -SecretKey $secret
Write-Host "Generated Token!" -ForegroundColor Green

# 3. Reassign Products
$targetShopIds = @("demo-shop-1", "69785193e54d760f00ecb62a")
$headers = @{ "Authorization" = "Bearer $token" }

try {
    $products = Invoke-RestMethod -Uri "$baseUrl/products" -Method Get -ErrorAction Stop
}
catch {
    Write-Host "Failed to fetch products." -ForegroundColor Red
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
            Invoke-RestMethod -Uri "$baseUrl/products/$($p.id)" -Method Put -Body $body -Headers $headers -ContentType "application/json" -ErrorAction Stop
            Write-Host " [OK]" -ForegroundColor Green
            $count++
        }
        catch {
            Write-Host " [FAILED]" -ForegroundColor Red
            Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
            if ($_.ErrorDetails) { Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Red }
        }
    }
}

Write-Host "------------------------------------------------"
Write-Host "Successfully moved $count products to Baribanai." -ForegroundColor Cyan
