# Hướng dẫn kết nối Spring Boot với MongoDB

## ✅ Đã hoàn thành

### 1. Thêm MongoDB Dependency
- Đã thêm `spring-boot-starter-data-mongodb` vào `pom.xml`

### 2. Cấu hình MongoDB
- Đã cấu hình trong `application.properties`
- Database name: `web_shop_db`
- Port mặc định: `27017`

### 3. Tạo cấu trúc code
- ✅ Model: `User.java`
- ✅ Repository: `UserRepository.java`
- ✅ Controller: `UserController.java`

---

## 📋 Các bước tiếp theo

### Bước 1: Cài đặt MongoDB (nếu chưa có)

#### Option A: MongoDB Local
1. Download MongoDB Community Server: https://www.mongodb.com/try/download/community
2. Cài đặt và chạy MongoDB service
3. Mặc định MongoDB sẽ chạy ở `localhost:27017`

#### Option B: MongoDB Atlas (Cloud - Miễn phí)
1. Đăng ký tài khoản tại: https://www.mongodb.com/cloud/atlas/register
2. Tạo cluster miễn phí
3. Lấy connection string
4. Cập nhật `application.properties`:
   ```properties
   spring.data.mongodb.uri=mongodb+srv://<username>:<password>@<cluster-url>/web_shop_db?retryWrites=true&w=majority
   ```

### Bước 2: Reload Maven Dependencies

#### Trong IntelliJ IDEA:
1. Click chuột phải vào `pom.xml`
2. Chọn `Maven` → `Reload project`

#### Trong Eclipse/STS:
1. Click chuột phải vào project
2. Chọn `Maven` → `Update Project`

#### Hoặc dùng command line:
```bash
mvn clean install -DskipTests
```

### Bước 3: Chạy ứng dụng

```bash
mvn spring-boot:run
```

Hoặc chạy file `DemoApplication.java` trong IDE

### Bước 4: Test kết nối MongoDB

Sau khi ứng dụng chạy thành công, mở browser hoặc Postman:

#### Test connection:
```
GET http://localhost:8080/api/users/test-connection
```

Nếu thành công, bạn sẽ thấy: `"Kết nối MongoDB thành công! Số lượng users: 0"`

---

## 🧪 Test các API endpoints

### 1. Tạo user mới (POST)
```bash
POST http://localhost:8080/api/users
Content-Type: application/json

{
    "username": "phongnha",
    "email": "phongnha@example.com",
    "password": "123456",
    "fullName": "Trần Đỗ Phong Nhã",
    "phone": "0123456789"
}
```

### 2. Lấy tất cả users (GET)
```bash
GET http://localhost:8080/api/users
```

### 3. Lấy user theo username (GET)
```bash
GET http://localhost:8080/api/users/username/phongnha
```

### 4. Lấy user theo ID (GET)
```bash
GET http://localhost:8080/api/users/{id}
```

### 5. Update user (PUT)
```bash
PUT http://localhost:8080/api/users/{id}
Content-Type: application/json

{
    "username": "phongnha",
    "email": "newemail@example.com",
    "fullName": "Trần Đỗ Phong Nhã Updated",
    "phone": "0987654321"
}
```

### 6. Xóa user (DELETE)
```bash
DELETE http://localhost:8080/api/users/{id}
```

---

## 🔧 Cấu hình MongoDB trong application.properties

### Local MongoDB:
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/web_shop_db
```

### MongoDB với authentication:
```properties
spring.data.mongodb.uri=mongodb://username:password@localhost:27017/web_shop_db?authSource=admin
```

### MongoDB Atlas (Cloud):
```properties
spring.data.mongodb.uri=mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/web_shop_db?retryWrites=true&w=majority
```

---

## 📁 Cấu trúc project

```
src/main/java/com/example/demo/
├── DemoApplication.java          # Main application
├── model/
│   └── User.java                 # MongoDB Document (Entity)
├── repository/
│   └── UserRepository.java       # MongoDB Repository
└── controller/
    └── UserController.java       # REST API Controller
```

---

## 🐛 Troubleshooting

### Lỗi: "Connection refused"
- Kiểm tra MongoDB service đã chạy chưa
- Kiểm tra port 27017 có bị block không

### Lỗi: "Authentication failed"
- Kiểm tra username/password trong connection string
- Kiểm tra authSource (thường là `admin`)

### Lỗi: Maven không tải được dependencies
- Kiểm tra internet connection
- Kiểm tra proxy settings trong `~/.m2/settings.xml`
- Thử reload project trong IDE

---

## 📚 Tài liệu tham khảo

- Spring Data MongoDB: https://spring.io/projects/spring-data-mongodb
- MongoDB Documentation: https://docs.mongodb.com/
- Spring Boot Reference: https://docs.spring.io/spring-boot/docs/current/reference/html/

---

## 💡 Tips

1. **Auto-index creation**: MongoDB sẽ tự động tạo index cho các field được đánh dấu `@Indexed`
2. **Lombok**: Đã sử dụng `@Data` để tự động generate getter/setter
3. **Repository**: Spring Data MongoDB tự động implement các method CRUD cơ bản
4. **Custom queries**: Có thể tạo custom query methods trong Repository interface

---

## ✨ Next Steps

Sau khi kết nối thành công, bạn có thể:
1. Tạo thêm các model khác (Product, Order, Category, etc.)
2. Implement authentication & authorization
3. Thêm validation cho các field
4. Tạo service layer để xử lý business logic
5. Implement pagination và sorting
