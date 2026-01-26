# Hướng Dẫn Sửa Lỗi IDE (Import Errors)

## Vấn Đề

IDE đang hiển thị các lỗi đỏ như:
- ❌ `The import org.springframework cannot be resolved`
- ❌ `ResponseEntity cannot be resolved to a type`
- ❌ `HttpStatus cannot be resolved to a variable`
- ❌ `@SpringBootApplication cannot be resolved to a type`

**Nhưng project BUILD THÀNH CÔNG!** ✅

## Nguyên Nhân

IDE (NetBeans/VS Code/IntelliJ) đang sử dụng **cache cũ** và chưa reload dependencies sau khi chúng ta fix Maven settings.xml. Project thực sự không có lỗi - chỉ là IDE cần refresh.

## Giải Pháp

### Bước 1: Reload Maven Project trong IDE

#### Nếu dùng **VS Code**:
1. Mở Command Palette: `Ctrl+Shift+P`
2. Gõ: `Java: Clean Java Language Server Workspace`
3. Chọn và nhấn Enter
4. Reload VS Code khi được hỏi

**HOẶC:**

1. Mở Command Palette: `Ctrl+Shift+P`
2. Gõ: `Java: Reload Projects`
3. Chọn và nhấn Enter

#### Nếu dùng **NetBeans**:
1. Click phải vào project `Web_shop`
2. Chọn `Reload Project` hoặc `Refresh`
3. Đợi NetBeans re-index project

#### Nếu dùng **IntelliJ IDEA**:
1. Click phải vào `pom.xml`
2. Chọn `Maven` → `Reload Project`
3. Hoặc nhấn nút "Reload All Maven Projects" (biểu tượng mũi tên tròn) trong Maven tool window

### Bước 2: Force Update Dependencies

Chạy lệnh này để force Maven download lại tất cả dependencies:

```bash
./mvnw dependency:purge-local-repository
./mvnw clean install
```

**HOẶC** đơn giản hơn:

```bash
./mvnw clean install -U
```

Flag `-U` sẽ force update tất cả dependencies.

### Bước 3: Xóa Cache IDE (Nếu vẫn còn lỗi)

#### VS Code:
```bash
# Đóng VS Code
# Xóa folder workspace storage
Remove-Item -Recurse -Force "$env:APPDATA\Code\User\workspaceStorage\*"
# Mở lại VS Code
```

#### NetBeans:
1. Đóng NetBeans
2. Xóa cache: `C:\Users\HPPAVILION\AppData\Local\NetBeans\Cache`
3. Mở lại NetBeans

### Bước 4: Verify Build

Chạy lại build để confirm:

```bash
./mvnw clean package
```

Nếu thấy `BUILD SUCCESS` → Project hoàn toàn OK! ✅

### Bước 5: Run Application

```bash
./mvnw spring-boot:run
```

Application sẽ chạy ngon lành trên port 8080.

## Kiểm Tra Dependencies

Để verify tất cả dependencies đã được download:

```bash
./mvnw dependency:tree
```

Lệnh này sẽ hiển thị toàn bộ dependency tree. Bạn sẽ thấy:
- `spring-boot-starter-web`
- `spring-boot-starter-data-mongodb`
- `lombok`
- và các dependencies khác

## Test Endpoints

Sau khi application chạy, test các endpoints:

### 1. Test MongoDB Connection
```bash
curl http://localhost:8080/api/users/test-connection
```

### 2. Get All Users
```bash
curl http://localhost:8080/api/users
```

### 3. Create User (POST)
```bash
curl -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d "{\"username\":\"test\",\"email\":\"test@example.com\",\"fullName\":\"Test User\",\"phone\":\"0123456789\"}"
```

## Lưu Ý Quan Trọng

> [!IMPORTANT]
> Các lỗi đỏ trong IDE **KHÔNG có nghĩa là code bị lỗi**. Nếu Maven build thành công (`BUILD SUCCESS`), code của bạn hoàn toàn OK. Chỉ cần reload IDE để nó nhận ra dependencies.

> [!TIP]
> Sau khi thay đổi `pom.xml` hoặc Maven settings, **LUÔN LUÔN** reload Maven project trong IDE để tránh các lỗi giả này.

## Troubleshooting

### Nếu vẫn thấy lỗi sau khi reload:

1. **Kiểm tra Java version**:
   ```bash
   java -version
   ```
   Phải là Java 21 (như trong pom.xml)

2. **Kiểm tra JAVA_HOME**:
   ```bash
   echo $env:JAVA_HOME
   ```
   Phải trỏ đến JDK 21

3. **Set JAVA_HOME nếu chưa có**:
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
   ```

4. **Restart IDE hoàn toàn** (đóng và mở lại)

## Kết Luận

✅ Project của bạn **KHÔNG CÓ LỖI**  
✅ Maven build **THÀNH CÔNG**  
✅ Chỉ cần **RELOAD IDE** để xóa các lỗi đỏ giả

Sau khi reload, tất cả các import sẽ được resolve và lỗi đỏ sẽ biến mất! 🎉
