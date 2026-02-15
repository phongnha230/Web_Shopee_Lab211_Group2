# Test upload ID card image to Cloudinary
# Run this after adding Cloudinary credentials to .env

$TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJwaG9uZ25oYTIzMEBnbWFpbC5jb20iLCJpYXQiOjE3Mzc5MjA5MzUsImV4cCI6MTczNzkyNDUzNX0.KmL3jHXuVMJRN9UkfWlbhTp5QT3-AxSKLKlnxaVZmLQNJjPM_4hgvGqJKQ93P-7h1rxTy8dYk9gKHFtjBXzpgw"

Write-Host "`n=== Test Cloudinary Image Upload ===" -ForegroundColor Cyan

# Create a dummy test image (1x1 pixel PNG)
$base64Image = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
$imageBytes = [Convert]::FromBase64String($base64Image)
$testImagePath = "test_id_card.png"
[IO.File]::WriteAllBytes($testImagePath, $imageBytes)

Write-Host "`n1. Uploading FRONT ID card image..." -ForegroundColor Yellow

# Create multipart form data
$boundary = [System.Guid]::NewGuid().ToString()
$LF = "`r`n"
$bodyLines = (
    "--$boundary",
    "Content-Disposition: form-data; name=`"file`"; filename=`"id_card_front.png`"",
    "Content-Type: image/png$LF",
    [System.Text.Encoding]::GetEncoding("iso-8859-1").GetString($imageBytes),
    "--$boundary",
    "Content-Disposition: form-data; name=`"type`"$LF",
    "front",
    "--$boundary--$LF"
) -join $LF

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/shop/upload-id-card" `
        -Method Post `
        -Headers @{
        "Authorization" = "Bearer $TOKEN"
        "Content-Type"  = "multipart/form-data; boundary=$boundary"
    } `
        -Body ([System.Text.Encoding]::GetEncoding("iso-8859-1").GetBytes($bodyLines))

    Write-Host "✅ Upload successful!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 3

    $frontUrl = $response.url
    Write-Host "`nCloudinary URL: $frontUrl" -ForegroundColor Green

}
catch {
    Write-Host "❌ Upload failed!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody" -ForegroundColor Yellow
    }
}

# Cleanup
Remove-Item $testImagePath -ErrorAction SilentlyContinue

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
