# Hướng Dẫn Kết Nối MongoDB vào Spring Boot

## Bước 1: Xác Định Thông Tin MongoDB 🔍

Từ hình ảnh MongoDB Compass của bạn, tôi thấy:

- **Connection Name**: `Jin1905`
- **Host**: `localhost` (local MongoDB)
- **Port**: `27017` (default port)
- **Database Name**: `web_shoppe` ⚠️ (Lưu ý: có 2 chữ "p")

## Bước 2: Lấy Connection String từ MongoDB Compass

### Cách 1: Xem Connection String trong Compass

1. Trong MongoDB Compass, click vào connection **"Jin1905"**
2. Click vào nút **"..."** (3 chấm) bên cạnh tên connection
3. Chọn **"Copy Connection String"**
4. Connection string sẽ có dạng: `mongodb://localhost:27017/`

### Cách 2: Tự tạo Connection String (Đơn giản hơn)

Với MongoDB local không có authentication, connection string sẽ là:

```
mongodb://localhost:27017/web_shoppe
```

**Giải thích các phần:**
- `mongodb://` - Protocol
- `localhost` - Host (máy local)
- `27017` - Port (default MongoDB port)
- `web_shoppe` - Tên database bạn muốn kết nối

## Bước 3: Cấu Hình application.properties

Mở file `application.properties` và **thay đổi dòng 12**:

### ❌ Cũ (Sai):
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/web_shop_db
```

### ✅ Mới (Đúng):
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/web_shoppe
```

**Lưu ý**: Đổi `web_shop_db` thành `web_shoppe` (đúng tên database trong Compass)

## Bước 4: File application.properties Hoàn Chỉnh

Sau khi sửa, file của bạn sẽ như thế này:

```properties
# Application Name
spring.application.name=demo

# Server Port Configuration
server.port=8080

# ========================================
# MongoDB Configuration
# ========================================

# Local MongoDB - Database: web_shoppe
spring.data.mongodb.uri=mongodb://localhost:27017/web_shoppe

# Option 2: MongoDB Atlas (Cloud) - Uncomment if using cloud
# spring.data.mongodb.uri=mongodb+srv://<username>:<password>@<cluster-url>/<database-name>?retryWrites=true&w=majority

# Additional MongoDB Settings (Optional)
# spring.data.mongodb.auto-index-creation=true
```

## Bước 5: Restart Application

Sau khi sửa `application.properties`, bạn cần **restart** Spring Boot application:

### Trong Terminal:

1. **Dừng application hiện tại**: Nhấn `Ctrl+C` trong terminal đang chạy
2. **Chạy lại**: 
   ```bash
   ./mvnw spring-boot:run
   ```

## Bước 6: Kiểm Tra Kết Nối

### Test 1: Xem Log Khi Start

Khi application start, bạn sẽ thấy log như:

```
INFO  o.s.d.m.c.MongoTemplate - Connecting to MongoDB at localhost:27017/web_shoppe
INFO  o.m.d.c.c.Cluster - Cluster created with settings ...
```

### Test 2: Gọi API Test Connection

Mở browser hoặc dùng curl:

```bash
curl http://localhost:8080/api/users/test-connection
```

**Kết quả mong đợi:**
```
Kết nối MongoDB thành công! Số lượng users: 0
```

(Số lượng có thể là 0 nếu collection `users` chưa có data)

### Test 3: Kiểm Tra trong MongoDB Compass

Sau khi chạy API, check trong MongoDB Compass:
1. Refresh database `web_shoppe`
2. Bạn sẽ thấy collection `users` được tạo tự động (nếu chưa có)

## Các Trường Hợp Đặc Biệt

### Nếu MongoDB Có Authentication

Nếu MongoDB của bạn có username/password:

```properties
spring.data.mongodb.uri=mongodb://username:password@localhost:27017/web_shoppe?authSource=admin
```

**Ví dụ:**
```properties
spring.data.mongodb.uri=mongodb://admin:123456@localhost:27017/web_shoppe?authSource=admin
```

### Nếu Dùng MongoDB Atlas (Cloud)

Nếu sau này bạn dùng MongoDB Atlas:

```properties
spring.data.mongodb.uri=mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/web_shoppe?retryWrites=true&w=majority
```

### Nếu Muốn Đổi Tên Database

Chỉ cần thay `web_shoppe` thành tên database khác:

```properties
# Ví dụ: Dùng database tên "my_shop"
spring.data.mongodb.uri=mongodb://localhost:27017/my_shop
```

## Cấu Hình Nâng Cao (Optional)

Nếu muốn cấu hình chi tiết hơn, bạn có thể tách riêng:

```properties
# Cách 1: Dùng URI (Recommended - Đơn giản)
spring.data.mongodb.uri=mongodb://localhost:27017/web_shoppe

# Cách 2: Tách riêng từng phần (Nâng cao)
# spring.data.mongodb.host=localhost
# spring.data.mongodb.port=27017
# spring.data.mongodb.database=web_shoppe
# spring.data.mongodb.username=admin
# spring.data.mongodb.password=123456
# spring.data.mongodb.authentication-database=admin
```

**Khuyến nghị**: Dùng **Cách 1** (URI) vì đơn giản và dễ quản lý hơn.

## Checklist Hoàn Thành ✅

- [ ] Đã xác định tên database trong MongoDB Compass: `web_shoppe`
- [ ] Đã sửa `application.properties` với connection string đúng
- [ ] Đã restart Spring Boot application
- [ ] Đã test API `/api/users/test-connection` và thấy kết quả thành công
- [ ] Đã kiểm tra trong MongoDB Compass thấy collection `users` được tạo

## Lưu Ý Quan Trọng ⚠️

> [!IMPORTANT]
> **Tên database phải khớp chính xác!**
> - Trong Compass: `web_shoppe` (2 chữ p)
> - Trong application.properties: `web_shoppe` (2 chữ p)
> - Nếu không khớp, Spring Boot sẽ tạo database mới!

> [!TIP]
> Sau khi kết nối thành công, Spring Boot sẽ **tự động tạo collections** khi bạn save data. Không cần tạo collection thủ công trong Compass.

## Troubleshooting

### Lỗi: "Connection refused"
- ✅ Check MongoDB đang chạy: Mở MongoDB Compass, nếu connect được là OK
- ✅ Check port: Default là 27017

### Lỗi: "Authentication failed"
- ✅ Nếu MongoDB không có password, bỏ username/password trong URI
- ✅ Nếu có password, thêm `?authSource=admin` vào cuối URI

### Lỗi: "Database not found"
- ✅ Không sao! MongoDB sẽ tự động tạo database khi bạn insert data lần đầu

## Ví Dụ Thực Tế

Sau khi kết nối thành công, bạn có thể:

### 1. Tạo User Mới
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "phongnha",
    "email": "phongnha@example.com",
    "password": "123456",
    "fullName": "Trần Đỗ Phong Nhã",
    "phone": "0123456789"
  }'
```

### 2. Xem Tất Cả Users
```bash
curl http://localhost:8080/api/users
```

### 3. Kiểm Tra trong MongoDB Compass
- Refresh database `web_shoppe`
- Click vào collection `users`
- Bạn sẽ thấy data vừa tạo!

---

**Tóm tắt nhanh:**
1. Sửa `application.properties` dòng 12: `mongodb://localhost:27017/web_shoppe`
2. Restart application: `Ctrl+C` rồi `./mvnw spring-boot:run`
3. Test: `curl http://localhost:8080/api/users/test-connection`
4. Done! ✅
