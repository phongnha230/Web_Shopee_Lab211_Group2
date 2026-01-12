# Hướng dẫn Import Dữ liệu CSV vào MongoDB

## 📦 Tổng quan

Bộ công cụ này giúp bạn:
1. Tạo file CSV với **10,000+ bản ghi** dữ liệu mẫu người dùng
2. Import dữ liệu từ CSV vào MongoDB thông qua REST API
3. Quản lý dữ liệu (xóa, đếm số lượng)

---

## 📁 Files đã tạo

### 1. Script Python tạo dữ liệu
- **File**: `generate_sample_data.py`
- **Chức năng**: Tạo file CSV với dữ liệu mẫu người dùng Việt Nam
- **Output**: `sample_users_data.csv` (10,000 bản ghi)

### 2. Java Utility Class
- **File**: `src/main/java/com/example/demo/util/CsvImporter.java`
- **Chức năng**: Xử lý import CSV vào MongoDB với batch processing

### 3. REST API Controller
- **File**: `src/main/java/com/example/demo/controller/ImportController.java`
- **Chức năng**: Cung cấp API endpoints để import và quản lý dữ liệu

---

## 🚀 Cách sử dụng

### Bước 1: Tạo dữ liệu CSV mẫu

```bash
# Chạy script Python
python generate_sample_data.py
```

**Kết quả**: File `sample_users_data.csv` được tạo với 10,000 bản ghi

**Cấu trúc CSV**:
```csv
username,email,password,fullName,phone
minh1,minh1@gmail.com,Password123!,Nguyễn Văn Minh,0901234567
...
```

### Bước 2: Khởi động Spring Boot Application

```bash
mvn spring-boot:run
```

Hoặc chạy `DemoApplication.java` trong IDE

### Bước 3: Import dữ liệu vào MongoDB

#### Option A: Import từ file trên server

```bash
POST http://localhost:8080/api/import/users/from-file?filePath=C:/Users/HPPAVILION/Downloads/Backend/Web_shop/sample_users_data.csv
```

**Response**:
```json
{
    "success": true,
    "message": "Import thành công!",
    "importedCount": 10000,
    "totalUsers": 10000
}
```

#### Option B: Upload file CSV qua API

```bash
POST http://localhost:8080/api/import/users/upload
Content-Type: multipart/form-data

file: [chọn file sample_users_data.csv]
```

---

## 🔧 API Endpoints

### 1. Import từ file trên server
```http
POST /api/import/users/from-file?filePath={đường_dẫn_file}
```

**Parameters**:
- `filePath`: Đường dẫn tuyệt đối tới file CSV

**Response**:
```json
{
    "success": true,
    "message": "Import thành công!",
    "importedCount": 10000,
    "totalUsers": 10000
}
```

### 2. Upload và import file CSV
```http
POST /api/import/users/upload
Content-Type: multipart/form-data
```

**Body**:
- `file`: File CSV (multipart/form-data)

### 3. Đếm số lượng users
```http
GET /api/import/users/count
```

**Response**:
```json
{
    "totalUsers": 10000
}
```

### 4. Xóa tất cả users (⚠️ Cẩn thận!)
```http
DELETE /api/import/users/clear
```

**Response**:
```json
{
    "success": true,
    "message": "Đã xóa tất cả users!",
    "deletedCount": 10000
}
```

---

## 📊 Thông tin dữ liệu mẫu

### Cấu trúc User
```java
{
    "id": "auto-generated",
    "username": "nguyenvanminh",
    "email": "nguyenvanminh@gmail.com",
    "password": "Password123!",
    "fullName": "Nguyễn Văn Minh",
    "phone": "0901234567"
}
```

### Đặc điểm dữ liệu
- **Họ tên**: Tên tiếng Việt có dấu (20 họ, 20 tên đệm, 40 tên)
- **Username**: Tự động tạo từ họ tên (không dấu)
- **Email**: 10 domain phổ biến (gmail, yahoo, outlook, etc.)
- **Password**: Mật khẩu mẫu đơn giản
- **Phone**: Số điện thoại Việt Nam hợp lệ (đầu số Viettel, Mobifone, Vinaphone)

---

## 🧪 Test với Postman

### 1. Import dữ liệu
```
POST http://localhost:8080/api/import/users/from-file
?filePath=C:/Users/HPPAVILION/Downloads/Backend/Web_shop/sample_users_data.csv
```

### 2. Kiểm tra số lượng
```
GET http://localhost:8080/api/import/users/count
```

### 3. Xem danh sách users
```
GET http://localhost:8080/api/users
```

---

## ⚡ Hiệu suất

- **Batch Processing**: Import theo lô 1,000 bản ghi
- **Tốc độ**: ~10,000 bản ghi trong vài giây (tùy cấu hình máy)
- **Memory**: Tối ưu hóa bộ nhớ với batch insert

---

## 🔍 Kiểm tra dữ liệu trong MongoDB

### Sử dụng MongoDB Compass
1. Kết nối tới `mongodb://localhost:27017`
2. Chọn database `web_shop_db`
3. Xem collection `users`

### Sử dụng MongoDB Shell
```bash
mongosh

use web_shop_db
db.users.countDocuments()  # Đếm số lượng
db.users.find().limit(10)  # Xem 10 bản ghi đầu
```

---

## 🛠️ Tùy chỉnh

### Thay đổi số lượng bản ghi

Sửa file `generate_sample_data.py`:
```python
# Tạo 50,000 bản ghi
generate_csv_data(50000)
```

### Thay đổi batch size

Sửa file `CsvImporter.java`:
```java
// Batch insert mỗi 5000 bản ghi
if (users.size() >= 5000) {
    userRepository.saveAll(users);
    // ...
}
```

---

## ⚠️ Lưu ý

1. **File CSV encoding**: UTF-8-BOM để hỗ trợ tiếng Việt
2. **Unique constraint**: Nếu có unique index trên username/email, có thể gặp lỗi duplicate
3. **Memory**: Import số lượng lớn (>100k) nên tăng batch size
4. **Backup**: Nên backup database trước khi import dữ liệu lớn

---

## 🐛 Troubleshooting

### Lỗi: "File không tồn tại"
- Kiểm tra đường dẫn file có đúng không
- Sử dụng đường dẫn tuyệt đối
- Trên Windows: `C:/Users/...` (dùng `/` thay vì `\`)

### Lỗi: "Connection refused"
- Kiểm tra MongoDB đã chạy chưa
- Kiểm tra connection string trong `application.properties`

### Lỗi: "Duplicate key"
- Xóa dữ liệu cũ: `DELETE /api/import/users/clear`
- Hoặc tạo username/email unique hơn

---

## 📚 Tham khảo

- [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb)
- [MongoDB Documentation](https://docs.mongodb.com/)
- [CSV Format Specification](https://tools.ietf.org/html/rfc4180)
