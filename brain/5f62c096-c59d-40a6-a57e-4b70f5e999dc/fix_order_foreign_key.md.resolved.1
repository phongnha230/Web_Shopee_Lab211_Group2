# 🔧 Quick Fix - Order API

## ❌ Vấn Đề

Bạn dùng `product_id: 1` nhưng database không có ID 1 (do đã xóa data cũ).

## ✅ Giải Pháp

### Bước 1: Lấy Product IDs Thực Tế

**GET Products trong Postman:**
```
GET http://localhost:5000/api/products
```

**Response:**
```json
[
  {"id": 14, "name": "Cà phê đen", "price": "25000.00"},
  {"id": 15, "name": "Cà phê sữa", "price": "30000.00"},
  {"id": 16, "name": "Bạc xỉu", "price": "35000.00"},
  {"id": 17, "name": "Nước cam", "price": "20000.00"}
]
```

### Bước 2: Dùng IDs Đúng Để Tạo Order

**Thay vì:**
```json
{
  "table_number": 5,
  "items": [
    {"product_id": 1, "quantity": 2, "unit_price": 45000}  // ❌
  ]
}
```

**Dùng:**
```json
{
  "table_number": 5,
  "items": [
    {"product_id": 14, "quantity": 2, "unit_price": 25000}  // ✅
  ]
}
```

## 💡 Ví Dụ Hoàn Chỉnh

**POST Create Order:**
```
POST http://localhost:5000/api/orders
Authorization: Bearer YOUR_TOKEN
Content-Type: application/json
```

**Body:**
```json
{
  "table_number": 5,
  "note": "Không đường",
  "items": [
    {"product_id": 14, "quantity": 2, "unit_price": 25000},
    {"product_id": 15, "quantity": 1, "unit_price": 30000}
  ]
}
```

**Expected:** 201 Created ✅

---

**Tóm lại:** Chỉ cần GET products trước, xem IDs nào có sẵn (14, 15, 16...), rồi dùng IDs đó thôi! 🚀
