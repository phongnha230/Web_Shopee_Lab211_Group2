# Script test User API - Không cần database
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TEST USER API (KHÔNG CẦN DATABASE)   " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Thêm user thứ nhất
Write-Host "1️⃣  Thêm user: Nguyen Van A" -ForegroundColor Yellow
$response1 = Invoke-WebRequest -Uri "http://localhost:8080/api/users" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body '{"username":"nguyenvana","email":"nguyenvana@gmail.com","fullName":"Nguyen Van A","phone":"0123456789"}' `
    -UseBasicParsing
Write-Host "✅ Kết quả:" -ForegroundColor Green
$response1.Content | ConvertFrom-Json | ConvertTo-Json
Write-Host ""

# 2. Thêm user thứ hai
Write-Host "2️⃣  Thêm user: Tran Thi B" -ForegroundColor Yellow
$response2 = Invoke-WebRequest -Uri "http://localhost:8080/api/users" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body '{"username":"tranthib","email":"tranthib@gmail.com","fullName":"Tran Thi B","phone":"0987654321"}' `
    -UseBasicParsing
Write-Host "✅ Kết quả:" -ForegroundColor Green
$response2.Content | ConvertFrom-Json | ConvertTo-Json
Write-Host ""

# 3. Thêm user thứ ba
Write-Host "3️⃣  Thêm user: Le Van C" -ForegroundColor Yellow
$response3 = Invoke-WebRequest -Uri "http://localhost:8080/api/users" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body '{"username":"levanc","email":"levanc@gmail.com","fullName":"Le Van C","phone":"0369852147"}' `
    -UseBasicParsing
Write-Host "✅ Kết quả:" -ForegroundColor Green
$response3.Content | ConvertFrom-Json | ConvertTo-Json
Write-Host ""

# 4. Lấy tất cả users
Write-Host "4️⃣  Lấy danh sách TẤT CẢ users" -ForegroundColor Yellow
$allUsers = Invoke-WebRequest -Uri "http://localhost:8080/api/users" `
    -Method GET `
    -UseBasicParsing
Write-Host "✅ Danh sách users hiện tại:" -ForegroundColor Green
$allUsers.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
Write-Host ""

# 5. Lấy user theo ID
Write-Host "5️⃣  Lấy user với ID = 2" -ForegroundColor Yellow
$userById = Invoke-WebRequest -Uri "http://localhost:8080/api/users/2" `
    -Method GET `
    -UseBasicParsing
Write-Host "✅ Kết quả:" -ForegroundColor Green
$userById.Content | ConvertFrom-Json | ConvertTo-Json
Write-Host ""

# 6. Cập nhật user
Write-Host "6️⃣  Cập nhật user ID = 1" -ForegroundColor Yellow
$updateUser = Invoke-WebRequest -Uri "http://localhost:8080/api/users/1" `
    -Method PUT `
    -Headers @{"Content-Type"="application/json"} `
    -Body '{"username":"nguyenvana_updated","email":"nguyenvana.new@gmail.com","fullName":"Nguyen Van A (Updated)","phone":"0999999999"}' `
    -UseBasicParsing
Write-Host "✅ Kết quả sau khi cập nhật:" -ForegroundColor Green
$updateUser.Content | ConvertFrom-Json | ConvertTo-Json
Write-Host ""

# 7. Xem lại danh sách sau khi update
Write-Host "7️⃣  Danh sách users sau khi cập nhật" -ForegroundColor Yellow
$allUsersAfterUpdate = Invoke-WebRequest -Uri "http://localhost:8080/api/users" `
    -Method GET `
    -UseBasicParsing
Write-Host "✅ Danh sách:" -ForegroundColor Green
$allUsersAfterUpdate.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
Write-Host ""

# 8. Xóa user
Write-Host "8️⃣  Xóa user ID = 3" -ForegroundColor Yellow
$deleteUser = Invoke-WebRequest -Uri "http://localhost:8080/api/users/3" `
    -Method DELETE `
    -UseBasicParsing
Write-Host "✅ User đã được xóa (Status Code: $($deleteUser.StatusCode))" -ForegroundColor Green
Write-Host ""

# 9. Xem danh sách cuối cùng
Write-Host "9️⃣  Danh sách users cuối cùng (sau khi xóa)" -ForegroundColor Yellow
$finalUsers = Invoke-WebRequest -Uri "http://localhost:8080/api/users" `
    -Method GET `
    -UseBasicParsing
Write-Host "✅ Danh sách cuối cùng:" -ForegroundColor Green
$finalUsers.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "        HOÀN THÀNH TEST API! ✅         " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
