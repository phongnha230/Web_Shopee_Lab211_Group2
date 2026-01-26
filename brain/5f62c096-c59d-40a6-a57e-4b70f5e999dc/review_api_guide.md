# 📝 Hướng Dẫn Test Review API - Postman

## 🎯 Tổng Quan

**Base URL:** `http://localhost:5000/api/reviews`

**Chức năng:** Khách hàng đánh giá sản phẩm (rating 1-5 sao + comment)

---

## 📋 Review API Routes

| Method | Endpoint | Auth? | Role | Description |
|--------|----------|-------|------|-------------|
| GET | `/api/reviews` | ❌ | Public | Lấy tất cả reviews |
| POST | `/api/reviews` | ✅ | Customer/Admin | Tạo review mới |
| GET | `/api/reviews/:id` | ✅ | Customer/Admin | Lấy review chi tiết |
| PUT | `/api/reviews/:id` | ✅ | Customer/Admin | Update review |
| DELETE | `/api/reviews/:id` | ✅ | Admin | Xóa review |

---

## 1️⃣ GET - Lấy Tất Cả Reviews (Public)

### Request

**Method:** `GET`  
**URL:** `http://localhost:5000/api/reviews`  
**Headers:** Không cần

### Query Parameters (Optional):

| Param | Type | Description |
|-------|------|-------------|
| `product_id` | Number | Filter reviews theo product |

### Examples:

**Lấy tất cả reviews:**
```
GET http://localhost:5000/api/reviews
```

**Lấy reviews của product ID 14:**
```
GET http://localhost:5000/api/reviews?product_id=14
```

### Expected Response (200 OK):
```json
[
  {
    "id": 1,
    "user_id": 2,
    "product_id": 14,
    "rating": 5,
    "comment": "Cà phê rất ngon, phục vụ tốt!",
    "created_at": "2025-12-15T10:00:00.000Z",
    "User": {
      "id": 2,
      "username": "customer1"
    }
  },
  {
    "id": 2,
    "user_id": 3,
    "product_id": 14,
    "rating": 4,
    "comment": "Ngon nhưng hơi đắt",
    "created_at": "2025-12-15T11:00:00.000Z",
    "User": {
      "id": 3,
      "username": "customer2"
    }
  }
]
```

---

## 2️⃣ POST - Tạo Review Mới

### Request

**Method:** `POST`  
**URL:** `http://localhost:5000/api/reviews`  
**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer YOUR_TOKEN`

**Body (JSON):**
```json
{
  "product_id": 14,
  "rating": 5,
  "comment": "Cà phê rất ngon, sẽ quay lại!"
}
```

### Fields:

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `product_id` | Number | ✅ | Phải tồn tại trong products |
| `rating` | Number | ✅ | 1-5 (số nguyên) |
| `comment` | String | ❌ | Text tùy ý |

### Expected Response (201 Created):
```json
{
  "id": 3,
  "user_id": 2,
  "product_id": 14,
  "rating": 5,
  "comment": "Cà phê rất ngon, sẽ quay lại!",
  "created_at": "2025-12-15T15:00:00.000Z"
}
```

### 💡 Test Cases:

**Review 5 sao:**
```json
{
  "product_id": 14,
  "rating": 5,
  "comment": "Xuất sắc! 10/10"
}
```

**Review 1 sao:**
```json
{
  "product_id": 14,
  "rating": 1,
  "comment": "Không ngon lắm"
}
```

**Review không có comment:**
```json
{
  "product_id": 15,
  "rating": 4
}
```

### ⚠️ Validation Errors:

**Rating ngoài phạm vi:**
```json
{
  "product_id": 14,
  "rating": 6  // ❌ Phải 1-5
}
```
**Response:** 400 Bad Request
```json
{
  "message": "rating must be 1..5"
}
```

**Đánh giá trùng:**
```json
// User đã review product này rồi
{
  "message": "Bạn đã đánh giá sản phẩm này rồi. Vui lòng sửa đánh giá thay vì tạo mới."
}
```

---

## 3️⃣ GET - Lấy Review Chi Tiết

### Request

**Method:** `GET`  
**URL:** `http://localhost:5000/api/reviews/1`  
**Headers:**
- `Authorization: Bearer YOUR_TOKEN`

### Expected Response (200 OK):
```json
{
  "id": 1,
  "user_id": 2,
  "product_id": 14,
  "rating": 5,
  "comment": "Cà phê rất ngon, phục vụ tốt!",
  "created_at": "2025-12-15T10:00:00.000Z"
}
```

---

## 4️⃣ PUT - Update Review

### Request

**Method:** `PUT`  
**URL:** `http://localhost:5000/api/reviews/1`  
**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer YOUR_TOKEN`

**Body (JSON):**
```json
{
  "rating": 4,
  "comment": "Sau khi suy nghĩ lại, 4 sao là hợp lý hơn"
}
```

### Expected Response (200 OK):
```json
{
  "id": 1,
  "user_id": 2,
  "product_id": 14,
  "rating": 4,
  "comment": "Sau khi suy nghĩ lại, 4 sao là hợp lý hơn",
  "created_at": "2025-12-15T10:00:00.000Z"
}
```

### 💡 Update Cases:

**Chỉ update rating:**
```json
{
  "rating": 3
}
```

**Chỉ update comment:**
```json
{
  "comment": "Comment mới"
}
```

**Update cả hai:**
```json
{
  "rating": 5,
  "comment": "Đã thử lại, rất tuyệt!"
}
```

---

## 5️⃣ DELETE - Xóa Review (Admin Only)

### Request

**Method:** `DELETE`  
**URL:** `http://localhost:5000/api/reviews/1`  
**Headers:**
- `Authorization: Bearer ADMIN_TOKEN` (⚠️ Chỉ admin)

### Expected Response (200 OK):
```json
{
  "success": true
}
```

---

## 🎯 Complete Test Workflow

### Scenario: Customer đánh giá sản phẩm

```
1. Login as Customer
   POST /api/users/login
   → Lấy token

2. Xem products để lấy product_id
   GET /api/products
   → Chọn product_id = 14

3. Tạo review
   POST /api/reviews
   Body: {
     "product_id": 14,
     "rating": 5,
     "comment": "Rất ngon!"
   }
   → Nhận review_id = 1

4. Xem tất cả reviews của product
   GET /api/reviews?product_id=14
   → Thấy review vừa tạo

5. Update review
   PUT /api/reviews/1
   Body: {
     "rating": 4,
     "comment": "Ngon nhưng hơi đắt"
   }
   → Review đã update

6. Xem lại review
   GET /api/reviews/1
   → Verify đã update
```

### Scenario: Admin quản lý reviews

```
1. Login as Admin
   POST /api/users/login (admin account)
   → Lấy admin token

2. Xem tất cả reviews
   GET /api/reviews
   → Thấy tất cả reviews

3. Xóa review không phù hợp
   DELETE /api/reviews/1
   → Xóa thành công
```

---

## 📊 Rating Statistics

Sau khi có nhiều reviews, bạn có thể tính trung bình:

**GET reviews của product:**
```
GET /api/reviews?product_id=14
```

**Response:**
```json
[
  {"rating": 5, "comment": "Tuyệt vời!"},
  {"rating": 4, "comment": "Ngon"},
  {"rating": 5, "comment": "Rất hài lòng"},
  {"rating": 3, "comment": "Bình thường"}
]
```

**Tính trung bình:** (5 + 4 + 5 + 3) / 4 = **4.25 sao** ⭐

---

## ✅ Test Checklist

### Basic Operations
- [ ] GET all reviews → 200 OK
- [ ] GET reviews by product_id → 200 OK
- [ ] POST create review (customer token) → 201 Created
- [ ] GET single review → 200 OK
- [ ] PUT update review → 200 OK
- [ ] DELETE review (admin token) → 200 OK

### Validation
- [ ] POST rating = 0 → 400 Bad Request
- [ ] POST rating = 6 → 400 Bad Request
- [ ] POST duplicate review → 400 Bad Request
- [ ] POST missing product_id → 400 Bad Request
- [ ] POST missing rating → 400 Bad Request

### Authorization
- [ ] POST without token → 401 Unauthorized
- [ ] DELETE with customer token → 403 Forbidden
- [ ] DELETE with admin token → 200 OK

### Edge Cases
- [ ] GET review không tồn tại → 404 Not Found
- [ ] PUT review không tồn tại → 404 Not Found
- [ ] DELETE review không tồn tại → 404 Not Found

---

## 🐛 Common Errors

### 400 Bad Request
```json
{"message": "product_id and numeric rating are required"}
```
**Fix:** Thêm `product_id` và `rating` vào body

### 400 Bad Request
```json
{"message": "rating must be 1..5"}
```
**Fix:** Đổi rating thành số từ 1-5

### 400 Bad Request
```json
{"message": "Bạn đã đánh giá sản phẩm này rồi..."}
```
**Fix:** Dùng PUT để update review cũ thay vì tạo mới

### 401 Unauthorized
```json
{"message": "Missing Authorization header"}
```
**Fix:** Thêm `Authorization: Bearer TOKEN` header

### 403 Forbidden
```json
{"message": "Access denied"}
```
**Fix:** Dùng đúng role (customer không thể DELETE)

### 404 Not Found
```json
{"message": "Not found"}
```
**Fix:** Kiểm tra review_id có tồn tại không

---

## 💡 Tips

### 1. Lấy Product IDs
```
GET /api/products
→ Note lại IDs để dùng cho reviews
```

### 2. Một User chỉ review một product một lần
- Nếu muốn thay đổi → dùng PUT update
- Không được tạo review mới cho cùng product

### 3. Rating Validation
- ✅ Hợp lệ: 1, 2, 3, 4, 5
- ❌ Không hợp lệ: 0, 6, 3.5, "5"

### 4. Comment Optional
- Có thể tạo review chỉ với rating
- Comment có thể null

---

**Chúc bạn test thành công! 🚀**
