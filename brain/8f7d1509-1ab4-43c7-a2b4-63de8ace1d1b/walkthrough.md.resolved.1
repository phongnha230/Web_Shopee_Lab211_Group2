# Hướng dẫn fix lỗi "non-project file"

## Vấn đề gốc rễ

Mày đang mở **SAI FOLDER** trong VS Code!

- ❌ **Đang mở**: `C:\Users\HPPAVILION\Downloads\Backend` (parent folder)
- ✅ **Cần mở**: `C:\Users\HPPAVILION\Downloads\Backend\src\Backend` (folder có `pom.xml`)

VS Code Java extension cần mở đúng folder chứa `pom.xml` để nhận diện Maven project.

## Cách fix (ĐÚNG NHẤT)

### Bước 1: Đóng workspace hiện tại
1. **File** → **Close Folder** (hoặc **Ctrl + K, F**)

### Bước 2: Mở đúng folder project
1. **File** → **Open Folder** (hoặc **Ctrl + K, Ctrl + O**)
2. Navigate đến: `C:\Users\HPPAVILION\Downloads\Backend\src\Backend`
3. Click **Select Folder**

### Bước 3: Đợi VS Code load project
- VS Code sẽ tự động detect `pom.xml`
- Java extension sẽ import Maven dependencies
- Đợi status bar hiện "Java: Ready" (góc dưới bên phải)

### Bước 4: Kiểm tra
- Mở file `User.java`
- Lỗi "non-project file" sẽ **BIẾN MẤT**
- Autocomplete, imports, và tất cả tính năng IDE sẽ hoạt động

---

## Cách fix thay thế (nếu muốn giữ parent folder)

Nếu mày muốn giữ workspace ở `Backend` (parent) để làm việc với nhiều projects, thì làm theo cách này:

### Tạo Multi-root Workspace

1. **File** → **Add Folder to Workspace...**
2. Chọn folder: `C:\Users\HPPAVILION\Downloads\Backend\src\Backend`
3. **File** → **Save Workspace As...**
4. Lưu file `.code-workspace` ở đâu đó

Sau đó mở file `.code-workspace` này thay vì mở folder trực tiếp.

---

## Tại sao cách trước không work?

Tao đã update settings ở `Backend/.vscode/settings.json` với:
```json
"java.project.sourcePaths": [
    "src/Backend/src/main/java"
]
```

Nhưng VS Code Java extension **KHÔNG THÍCH** nested paths như vậy. Nó muốn:
- Workspace root = Maven project root (folder có `pom.xml`)
- Source paths = relative paths từ project root (`src/main/java`)

## Khuyến nghị

**Mở lại VS Code với đúng folder project** - đây là cách đơn giản và chuẩn nhất!

```
C:\Users\HPPAVILION\Downloads\Backend\src\Backend
```

Sau khi mở đúng folder, mọi thứ sẽ work ngay lập tức! 🚀
