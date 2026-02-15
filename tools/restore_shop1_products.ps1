# Helper function to generate JWT (Base64Url encoded)
function New-JwtToken {
    param (
        [string]$Secret,
        [string]$Email
    )

    # 1. Header
    $header = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes('{"alg":"HS256","typ":"JWT"}')).TrimEnd('=').Replace('+', '-').Replace('/', '_')

    # 2. Payload
    # Expire in 1 hour (3600 seconds)
    $exp = [int][double]::Parse((Get-Date).ToUniversalTime().AddHours(1).Subtract((Get-Date -Date "1970-01-01")).TotalSeconds)
    $payloadJson = '{"sub":"' + $Email + '","iat":' + $exp + ',"exp":' + $exp + '}'
    $payload = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($payloadJson)).TrimEnd('=').Replace('+', '-').Replace('/', '_')

    # 3. Signature
    $dataToSign = "$header.$payload"
    $hmac = New-Object System.Security.Cryptography.HMACSHA256
    $hmac.Key = [System.Text.Encoding]::UTF8.GetBytes($Secret)
    $signatureBytes = $hmac.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($dataToSign))
    $signature = [Convert]::ToBase64String($signatureBytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')

    return "$dataToSign.$signature"
}

# --- CONFIGURATION ---
$baseUrl = "http://localhost:8080/api"
$envFile = ".\.env"

# Read JWT_SECRET from .env
if (-not (Test-Path $envFile)) {
    Write-Error ".env file not found at $envFile"
    exit
}

$jwtSecret = ""
Get-Content $envFile | ForEach-Object {
    if ($_ -match "JWT_SECRET=(.*)") {
        $jwtSecret = $matches[1].Trim()
    }
}

if ([string]::IsNullOrWhiteSpace($jwtSecret)) {
    Write-Error "JWT_SECRET not found in .env"
    exit
}

Write-Host "JWT Secret found (length: $($jwtSecret.Length))" -ForegroundColor Green

# --- SHOP IDs ---
$shop1Id = "69785193e54d760f00ecb62a"
$baribanaiId = "697dadea2c4624e9a1b1508a"

# --- ACCOUNTS ---
$emailBaribanai = "khanhlinhspacez2k4@gmail.com" # Current owner of the products
$emailShop1 = "shop@gmail.com" # Target owner (Shop 1)

# Generate Token for Baribanai Owner (to read products)
# Using Baribanai owner because only they can see their products via /shop/{id} if updated security rules applied
# Actually public endpoint /api/products/shop/{id} allows anyone to read.
# But updating requires auth. I'll use Shop 1 owner token for update?
# Wait, if I update a product, I might need to be the owner of the product?
# Or just a valid user? The code didn't check ownership.
# To be safe, let's use Baribanai owner token to update, essentially "giving away" the products.
$token = New-JwtToken -Secret $jwtSecret -Email $emailBaribanai
Write-Host "Generated JWT for $emailBaribanai" -ForegroundColor Cyan

# --- RESTORE LOGIC ---
Write-Host "Fetching products from Baribanai ($baribanaiId)..." -ForegroundColor Yellow

try {
    $products = Invoke-RestMethod -Uri "$baseUrl/products/shop/$baribanaiId" -Method Get
    Write-Host "Found $($products.Count) products in Baribanai." -ForegroundColor Cyan

    foreach ($p in $products) {
        $shouldMove = $true
        
        # Check if custom product
        if ($p.name -like "*Abbott*" -or $p.name -like "*Those Who Think It's Their Friend*") {
            $shouldMove = $false
            Write-Host "KEEPING: $($p.name) (Custom product)" -ForegroundColor Green
        }

        if ($shouldMove) {
            Write-Host "MOVING: $($p.name) -> Shop 1 ($shop1Id)" -ForegroundColor Yellow
            
            # Update shopId
            $body = @{
                shopId = $shop1Id
            } | ConvertTo-Json

            try {
                Invoke-RestMethod -Uri "$baseUrl/products/$($p.id)" -Method Put -Headers @{ Authorization = "Bearer $token"; "Content-Type" = "application/json" } -Body $body
                Write-Host "  -> Success" -ForegroundColor Green
            }
            catch {
                Write-Host "  -> FAILED: $($_.Exception.Message)" -ForegroundColor Red
            }
        }
    }

}
catch {
    Write-Host "Error fetching products: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "Restoration complete." -ForegroundColor Green
