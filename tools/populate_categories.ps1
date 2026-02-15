# Script to populate ProductCategory mappings
Write-Host "Starting..." -ForegroundColor Cyan

# Get categories
$catResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/categories" -UseBasicParsing
$categories = $catResponse.Content | ConvertFrom-Json
$categoryMap = @{}
foreach ($cat in $categories) {
    $categoryMap[$cat.name] = $cat.id
}
Write-Host "Categories loaded: $($categories.Count)"

# Get products
$prodResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/products" -UseBasicParsing
$products = $prodResponse.Content | ConvertFrom-Json
Write-Host "Products loaded: $($products.Count)"

# Category detection
function Get-Category {
    param($name)
    $n = $name.ToLower()
    if ($n -match "shoe|sneaker|nike|adidas|watch") { return "Fashion" }
    if ($n -match "phone|samsung") { return "Mobile & Gadgets" }
    if ($n -match "headphone|speaker") { return "Electronics" }
    if ($n -match "chair") { return "Home" }
    if ($n -match "sport|running") { return "Sports" }
    return "Electronics"
}

# Assign
$count = 0
foreach ($product in $products) {
    $category = Get-Category -name $product.name
    $categoryId = $categoryMap[$category]
    
    if ($categoryId) {
        $url = "http://localhost:8080/api/products/$($product.id)/categories/$categoryId"
        try {
            Invoke-WebRequest -Uri $url -Method POST -UseBasicParsing | Out-Null
            Write-Host "OK: $($product.name) -> $category" -ForegroundColor Green
            $count++
        }
        catch {
            Write-Host "SKIP: $($product.name) (may already exist)" -ForegroundColor Yellow
        }
    }
}

Write-Host "Completed: $count products assigned" -ForegroundColor Green

# Verify
$mapResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/debug/product-categories" -UseBasicParsing
$mappings = $mapResponse.Content | ConvertFrom-Json
Write-Host "Total mappings in DB: $($mappings.Count)" -ForegroundColor Cyan
