
$ErrorActionPreference = "Stop"

try {
    $content = Get-Content -Path index.html -Raw -Encoding UTF8
    $newContent = $content -replace 'orange-', 'red-' -replace '#FB5533', '#DC2626' -replace '#fa4724', '#B91C1C'
    
    $tempFile = "index_temp_$(Get-Random).html"
    Set-Content -Path $tempFile -Value $newContent -Encoding UTF8
    
    Write-Host "Attempting to overwrite index.html..."
    
    # Try Move-Item first
    try {
        Move-Item -Path $tempFile -Destination index.html -Force
        Write-Host "Success via Move-Item"
    } catch {
        Write-Host "Move-Item failed, trying cmd move..."
        cmd /c move /y $tempFile index.html
        if ($LASTEXITCODE -ne 0) {
            throw "CMD move failed"
        }
        Write-Host "Success via CMD move"
    }

} catch {
    Write-Error "Script failed: $_"
    exit 1
}
