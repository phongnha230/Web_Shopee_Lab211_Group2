# Giải Quyết Lỗi 500 - News API

## 🔍 Vấn Đề

![Error Screenshot](C:/Users/HPPAVILION/.gemini/antigravity/brain/5f62c096-c59d-40a6-a57e-4b70f5e999dc/error_screenshot.png)

Từ screenshot bạn cung cấp, API `/api/news` đang trả về lỗi **500 Internal Server Error** với các thông báo:
- `Failed to load resource: the server responded with a status of 500 (Internal Server Error)`
- `Load news from API failed`
- `AxiosError` với message "Request failed with status code 500"

## 🎯 Nguyên Nhân Chính

Sau khi phân tích code, tôi đã tìm ra **2 vấn đề chính**:

### 1. **Model News thiếu field `status`**

**File:** [`backend/models/news.js`](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/backend/models/news.js)

**Vấn đề:**
- Controller [`newController.js`](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/backend/controllers/newController.js) đang sử dụng field `status` (dòng 17, 21, 84, 105, 117)
- Nhưng model News **KHÔNG có** field `status` được định nghĩa
- Khi query database với field không tồn tại → **500 error**

**Code lỗi trong controller:**
```javascript
const { page = 1, limit = 10, status } = req.query;
const where = {};
if (status) where.status = status; // ❌ Field 'status' không tồn tại trong model
```

### 2. **Thiếu field `updated_at` trong model**

**Vấn đề:**
- Model định nghĩa `timestamps: true` và `updatedAt: 'updated_at'`
- Nhưng không có field `updated_at` trong schema
- Sequelize sẽ tự động tạo field này, nhưng tốt hơn là khai báo rõ ràng

## ✅ Giải Pháp Đã Áp Dụng

### Fix 1: Thêm field `status` vào News Model

**File:** [`backend/models/news.js`](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/backend/models/news.js)

```diff
const News = sequelize.define('News', {
  id: { type: DataTypes.INTEGER, primaryKey: true, autoIncrement: true },
  title: { type: DataTypes.STRING(255), allowNull: false },
  content: { type: DataTypes.TEXT, allowNull: false },
  image_url: { type: DataTypes.STRING(255), allowNull: true },
+ status: { 
+   type: DataTypes.ENUM('draft', 'published', 'archived'), 
+   defaultValue: 'draft',
+   allowNull: false 
+ },
  created_by: { type: DataTypes.INTEGER, allowNull: false },
  created_at: { type: DataTypes.DATE, defaultValue: DataTypes.NOW },
+ updated_at: { type: DataTypes.DATE, defaultValue: DataTypes.NOW },
  is_pinned: { type: DataTypes.BOOLEAN, defaultValue: false }
}, {
  tableName: 'news',
  timestamps: true,
  createdAt: 'created_at',
  updatedAt: 'updated_at',
});
```

**Giải thích:**
- ✅ Thêm field `status` với 3 giá trị: `draft`, `published`, `archived`
- ✅ Mặc định là `draft`
- ✅ Thêm field `updated_at` để đồng bộ với timestamps config

### Fix 2: Cải thiện Error Logging

**File:** [`backend/controllers/newController.js`](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/backend/controllers/newController.js)

```diff
} catch (error) {
  console.error('Error fetching news:', error);
+ console.error('Error details:', {
+   message: error.message,
+   stack: error.stack,
+   name: error.name
+ });
- res.status(500).json({ message: 'Error fetching news' });
+ res.status(500).json({ 
+   message: 'Error fetching news',
+   error: process.env.NODE_ENV === 'development' ? error.message : undefined
+ });
}
```

**Lợi ích:**
- ✅ Log chi tiết hơn để debug
- ✅ Trả về error message trong development mode
- ✅ Ẩn error details trong production (bảo mật)

## 🚀 Các Bước Tiếp Theo

### 1. **Restart Backend Server**

Bạn cần restart backend server để áp dụng thay đổi:

```bash
cd backend
npm run dev
```

Hoặc nếu đang chạy:
1. Dừng server (Ctrl+C)
2. Chạy lại: `npm run dev`

### 2. **Sync Database Schema**

Sequelize sẽ tự động sync khi server khởi động (do có `sequelize.sync()` trong [`app.js`](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/backend/app.js#L78-L84))

**Lưu ý:** Nếu bảng `news` đã tồn tại, bạn có thể cần:

**Option A - Force Sync (XÓA DỮ LIỆU CŨ):**
```javascript
// Trong app.js, tạm thời thay đổi:
sequelize.sync({ force: true }) // ⚠️ Sẽ xóa hết data
```

**Option B - Alter Sync (GIỮ DỮ LIỆU):**
```javascript
// Trong app.js, tạm thời thay đổi:
sequelize.sync({ alter: true }) // ✅ Giữ data, chỉ thêm column
```

**Option C - Manual SQL (KHUYẾN NGHỊ):**
```sql
ALTER TABLE news 
ADD COLUMN status ENUM('draft', 'published', 'archived') 
DEFAULT 'draft' NOT NULL AFTER image_url;

ALTER TABLE news 
ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP 
ON UPDATE CURRENT_TIMESTAMP AFTER created_at;
```

### 3. **Test API**

Sau khi restart, test lại:

```bash
# Test GET all news
curl http://localhost:5000/api/news

# Test GET with status filter
curl http://localhost:5000/api/news?status=published
```

## 📊 Kiểm Tra Kết Quả

Sau khi áp dụng fix, bạn sẽ thấy:

✅ **Trước:**
```
❌ GET http://localhost:5000/api/news → 500 Internal Server Error
❌ Load news from API failed
```

✅ **Sau:**
```
✅ GET http://localhost:5000/api/news → 200 OK
✅ {
  "data": [...],
  "pagination": {
    "total": 0,
    "page": 1,
    "totalPages": 0
  }
}
```

## 🔧 Các Vấn Đề Khác Có Thể Gặp

### 1. Database Connection Error

Nếu vẫn lỗi, kiểm tra:
- MySQL đang chạy trên port 3307
- Database `coffeeshop` đã tồn tại
- Credentials trong [`.env`](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/backend/.env) đúng

### 2. CORS Error

Nếu frontend vẫn không load được:
- Kiểm tra `FRONTEND_URL` trong backend `.env` = `http://localhost:5173`
- Kiểm tra `VITE_API_BASE_URL` trong frontend `.env` = `http://localhost:5000/api`

### 3. Association Error

Nếu lỗi liên quan đến `author`:
- File [`associations.js`](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/backend/models/associations.js#L64-L68) đã có association đúng
- Đảm bảo User model đã được load

## 📝 Tóm Tắt

**Root Cause:** Model News thiếu field `status` mà controller đang sử dụng

**Solution:** 
1. ✅ Thêm field `status` và `updated_at` vào News model
2. ✅ Cải thiện error logging
3. 🔄 Restart backend server
4. 🔄 Sync database schema

**Next Steps:**
- Restart backend server
- Sync database (alter hoặc manual SQL)
- Test API endpoint
