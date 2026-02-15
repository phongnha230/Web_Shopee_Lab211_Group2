# Test Category Filtering API
# This script tests the category filtering functionality

$baseUrl = "http://localhost:8080/api"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing Category Filtering System" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Step 1: Get all categories
Write-Host "[1] Fetching all categories..." -ForegroundColor Yellow
try {
    $categoriesResponse = Invoke-RestMethod -Uri "$baseUrl/categories" -Method Get
    Write-Host "Success: Found $($categoriesResponse.Count) categories" -ForegroundColor Green
    
    # Display categories
    $categoriesResponse | ForEach-Object {
        Write-Host "  - ID: $($_.id) | Name: $($_.name)" -ForegroundColor White
    }
    Write-Host ""
}
catch {
    Write-Host "Failed to fetch categories: $_" -ForegroundColor Red
    exit 1
}

# Step 2: Test each category
Write-Host "[2] Testing product filtering for each category..." -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

foreach ($category in $categoriesResponse) {
    $categoryId = $category.id
    $categoryName = $category.name
    
    Write-Host "Testing Category: $categoryName (ID: $categoryId)" -ForegroundColor Magenta
    
    try {
        $productsResponse = Invoke-RestMethod -Uri "$baseUrl/products/category/$categoryId" -Method Get
        
        if ($productsResponse.Count -eq 0) {
            Write-Host "  Warning: No products found in this category" -ForegroundColor Yellow
        }
        else {
            Write-Host "  Success: Found $($productsResponse.Count) product(s)" -ForegroundColor Green
            
            # Display first 3 products
            $productsResponse | Select-Object -First 3 | ForEach-Object {
                Write-Host "    Product: $($_.name) (Shop: $($_.shopId))" -ForegroundColor White
            }
            
            if ($productsResponse.Count -gt 3) {
                Write-Host "    ... and $($productsResponse.Count - 3) more" -ForegroundColor Gray
            }
        }
    }
    catch {
        Write-Host "  Error fetching products: $_" -ForegroundColor Red
    }
    
    Write-Host ""
}

# Step 3: Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$categoriesWithProducts = 0
$categoriesWithoutProducts = 0

foreach ($category in $categoriesResponse) {
    try {
        $productsResponse = Invoke-RestMethod -Uri "$baseUrl/products/category/$($category.id)" -Method Get
        if ($productsResponse.Count -gt 0) {
            $categoriesWithProducts++
        }
        else {
            $categoriesWithoutProducts++
        }
    }
    catch {
        # Ignore errors in summary
    }
}

Write-Host "Total Categories: $($categoriesResponse.Count)" -ForegroundColor White
Write-Host "Categories with Products: $categoriesWithProducts" -ForegroundColor Green
Write-Host "Categories without Products: $categoriesWithoutProducts" -ForegroundColor Yellow
Write-Host "`nTest completed!" -ForegroundColor Cyan
