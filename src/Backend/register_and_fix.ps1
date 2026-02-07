$baseUrl = "http://localhost:8080/api"
$baribanaiId = "697dadea2c4624e9a1b1508a"
$secret = "YourSuperSecretKeyForJWTTokenGenerationAndValidation123456" # From .env
$email = "admin_fix_$(Get-Random)@example.com"
$password = "Password123"

# 1. Register User
Write-Host "Registering User: $email" -ForegroundColor Cyan
$registerBody = @{
    email       = $email
    password    = $password
    fullName    = "Admin Fix"
    phoneNumber = "0909090909"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $registerBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "User Registered!" -ForegroundColor Green
}
catch {
    Write-Host "Registration Failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails) { Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red }
    # Proceeding anyway just in case it failed because user exists (unlikely with random)
}

# 2. Generate JWT
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

$token = New-JwtToken -Email $email -SecretKey $secret
Write-Host "Generated Token!" -ForegroundColor Green

# 3. Reassign Products
$targetShopIds = @("demo-shop-1", "69785193e54d760f00ecb62a") # Including Shop Cong Nghe So 1
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
            # Dump full exception response if available
            try { 
                $stream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                Write-Host "  Body: $($reader.ReadToEnd())" -ForegroundColor Red
            }
            catch {}
        }
    }
}

Write-Host "------------------------------------------------"
Write-Host "Successfully moved $count products to Baribanai." -ForegroundColor Cyan
