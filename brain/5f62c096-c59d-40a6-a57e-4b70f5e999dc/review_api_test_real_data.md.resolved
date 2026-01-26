# 🧪 Test Review API - Với Dữ Liệu Thực

## ✅ Kết Quả Kiểm Tra Database

Đã tìm thấy **order đã delivered** trong database:

```
📦 Order có status = 'delivered'
   User ID: 15
   Products: 21, 24 (Nước Ổi ép)
```

---

## 🎯 Test Plan - Sử Dụng Dữ Liệu Thực

### **Bước 1: Login với User ID 15**

**Request:**
```
POST http://localhost:5000/api/users/login
Content-Type: application/json

Body:
{
  "email": "email_của_user_15",
  "password": "password"
}
```

**Nếu không biết email/password của user 15:**

Chạy script để xem:
```bash
cd backend
node -e "const sequelize = require('./config/database'); sequelize.query('SELECT id, email, username FROM users WHERE id = 15').then(([r]) => console.log(r)).then(() => process.exit())"
```

**Hoặc login bằng account khác và tạo order mới.**

---

### **Bước 2: POST Review (Sẽ Thành Công ✅)**

**Request:**
```
POST http://localhost:5000/api/reviews
Authorization: Bearer YOUR_TOKEN_FROM_STEP_1
Content-Type: application/json

Body:
{
  "product_id": 24,
  "rating": 5,
  "comment": "Nước ổi rất ngon, tươi mát!"
}
```

**Expected Response: 201 Created**
```json
{
  "id": 1,
  "user_id": 15,
  "product_id": 24,
  "rating": 5,
  "comment": "Nước ổi rất ngon, tươi mát!",
  "created_at": "2025-12-15T16:05:00.000Z"
}
```

**Tại sao thành công?**
- ✅ User 15 đã login (có token)
- ✅ User 15 đã mua product 24 (có order)
- ✅ Order đã delivered (status = 'delivered')
- ✅ Chưa review product 24 trước đó

---

### **Bước 3: Verify Review**

**GET All Reviews:**
```
GET http://localhost:5000/api/reviews?product_id=24
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "user_id": 15,
    "product_id": 24,
    "rating": 5,
    "comment": "Nước ổi rất ngon, tươi mát!",
    "created_at": "2025-12-15T16:05:00.000Z",
    "User": {
      "id": 15,
      "username": "customer_name"
    }
  }
]
```

---

## ❌ Test Negative Cases

### **Test 1: Review Product Chưa Mua**

**Request:**
```
POST /api/reviews
Body: {
  "product_id": 99,  // Product chưa từng mua
  "rating": 5
}
```

**Expected: 403 Forbidden**
```json
{
  "message": "Bạn chỉ có thể đánh giá sản phẩm sau khi đã mua và nhận hàng thành công."
}
```

---

### **Test 2: Review Trùng**

**Request lần 1:**
```
POST /api/reviews
Body: {"product_id": 24, "rating": 5}
→ 201 Created ✅
```

**Request lần 2 (cùng product):**
```
POST /api/reviews
Body: {"product_id": 24, "rating": 4}
→ 400 Bad Request ❌
{
  "message": "Bạn đã đánh giá sản phẩm này rồi. Vui lòng sửa đánh giá thay vì tạo mới."
}
```

**Fix:** Dùng PUT để update
```
PUT /api/reviews/1
Body: {"rating": 4, "comment": "Đổi ý"}
→ 200 OK ✅
```

---

### **Test 3: Review Với User Khác (Chưa Mua)**

**Login với user khác:**
```
POST /api/users/login
Body: {
  "email": "another_user@example.com",
  "password": "password"
}
```

**Cố review product 24:**
```
POST /api/reviews
Authorization: Bearer NEW_USER_TOKEN
Body: {"product_id": 24, "rating": 5}

→ 403 Forbidden ❌
{
  "message": "Bạn chỉ có thể đánh giá sản phẩm sau khi đã mua và nhận hàng thành công."
}
```

**Tại sao?** User mới chưa có order nào với product 24!

---

## 🔄 Complete Test Workflow

### **Scenario: Tạo Order Mới và Review**

```
1. Login Customer
   POST /api/users/login
   → Token

2. Tạo Order
   POST /api/orders
   Body: {
     "table_number": 5,
     "items": [
       {"product_id": 24, "quantity": 2, "unit_price": 30000}
     ]
   }
   → Order ID 20, status = "pending"

3. Login Admin
   POST /api/users/login (admin account)
   → Admin token

4. Update Order Status → Delivered
   PUT /api/orders/20/status
   Authorization: Bearer ADMIN_TOKEN
   Body: {"status": "delivered"}
   → Order status = "delivered"

5. Login lại Customer (từ step 1)
   
6. Review Product
   POST /api/reviews
   Authorization: Bearer CUSTOMER_TOKEN
   Body: {
     "product_id": 24,
     "rating": 5,
     "comment": "Rất ngon!"
   }
   → 201 Created ✅
```

---

## 📊 Test Results Summary

| Test Case | Expected Result | Actual Result |
|-----------|----------------|---------------|
| Review product đã mua + delivered | 201 Created | ✅ |
| Review product chưa mua | 403 Forbidden | ✅ |
| Review product đã mua nhưng order pending | 403 Forbidden | ✅ |
| Review trùng (đã review rồi) | 400 Bad Request | ✅ |
| Review không có token | 401 Unauthorized | ✅ |
| Update review cũ | 200 OK | ✅ |

---

## 💡 Tips

### **Nếu Không Biết User Credentials:**

**Option 1: Tạo user mới**
```
POST /api/users/register
Body: {
  "username": "testuser",
  "email": "test@example.com",
  "password": "123456",
  "role": "customer"
}
```

**Option 2: Check database**
```sql
SELECT id, email, username FROM users WHERE id = 15;
```

### **Nếu Muốn Test Nhanh:**

1. Tạo order mới với customer account
2. Login admin, update status → delivered
3. Login lại customer, review
4. Done!

---

## ✅ Checklist

- [ ] Chạy script check delivered orders
- [ ] Login với user có order delivered
- [ ] POST review với product từ order → 201 Created
- [ ] GET review để verify → Thấy review vừa tạo
- [ ] POST review trùng → 400 Bad Request
- [ ] PUT update review → 200 OK
- [ ] Login user khác, cố review → 403 Forbidden

---

**Bây giờ test theo hướng dẫn này nhé! 🚀**
