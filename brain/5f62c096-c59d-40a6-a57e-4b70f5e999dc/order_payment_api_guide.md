# 📦 Hướng Dẫn Test Order & Payment API - Postman

## 🎯 Tổng Quan

### Order API - Quản lý đơn hàng
- **Base URL:** `http://localhost:5000/api/orders`
- **Auth:** ✅ Bắt buộc (Customer hoặc Admin)
- **Features:** Tạo order, xem danh sách, xem chi tiết, update status

### Payment API - Quản lý thanh toán
- **Base URL:** `http://localhost:5000/api/payments`
- **Auth:** ✅ Bắt buộc (Customer hoặc Admin)
- **Features:** Tạo payment, xem danh sách, update, xóa

---

## 🔐 Bước Chuẩn Bị

### 1. Login để lấy Token

**Method:** `POST`  
**URL:** `http://localhost:5000/api/users/login`  
**Body (JSON):**
```json
{
  "email": "customer@example.com",
  "password": "your_password"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 2,
    "username": "customer",
    "email": "customer@example.com",
    "role": "customer"
  }
}
```

**📝 Lưu token này để dùng cho tất cả requests!**

---

# 📦 ORDER API

## 1️⃣ POST - Tạo Order Mới

### Request

**Method:** `POST`  
**URL:** `http://localhost:5000/api/orders`  
**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer YOUR_TOKEN`

**Body (JSON):**
```json
{
  "table_number": 5,
  "note": "Không đường, ít đá",
  "items": [
    {
      "product_id": 1,
      "quantity": 2,
      "unit_price": 45000
    },
    {
      "product_id": 2,
      "quantity": 1,
      "unit_price": 35000
    }
  ]
}
```

### Giải Thích Fields:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `table_number` | Number | ❌ | Số bàn (có thể null nếu order mang đi) |
| `note` | String | ❌ | Ghi chú đặc biệt |
| `items` | Array | ✅ | Danh sách sản phẩm (tối thiểu 1 item) |
| `items[].product_id` | Number | ✅ | ID sản phẩm |
| `items[].quantity` | Number | ✅ | Số lượng |
| `items[].unit_price` | Number | ✅ | Giá đơn vị |

### Expected Response (201 Created):
```json
{
  "order": {
    "id": 1,
    "user_id": 2,
    "table_number": 5,
    "status": "pending",
    "note": "Không đường, ít đá",
    "total_amount": 125000,
    "created_at": "2025-12-12T09:00:00.000Z"
  },
  "items": [
    {
      "order_id": 1,
      "product_id": 1,
      "quantity": 2,
      "unit_price": 45000,
      "subtotal": 90000
    },
    {
      "order_id": 1,
      "product_id": 2,
      "quantity": 1,
      "unit_price": 35000,
      "subtotal": 35000
    }
  ]
}
```

### 💡 Test Cases:

**Order mang đi (không có bàn):**
```json
{
  "note": "Giao tận nơi",
  "items": [
    {
      "product_id": 1,
      "quantity": 1,
      "unit_price": 45000
    }
  ]
}
```

**Order nhiều items:**
```json
{
  "table_number": 3,
  "items": [
    {"product_id": 1, "quantity": 2, "unit_price": 45000},
    {"product_id": 2, "quantity": 3, "unit_price": 35000},
    {"product_id": 3, "quantity": 1, "unit_price": 50000}
  ]
}
```

---

## 2️⃣ GET - Lấy Danh Sách Orders

### Request

**Method:** `GET`  
**URL:** `http://localhost:5000/api/orders`  
**Headers:**
- `Authorization: Bearer YOUR_TOKEN`

### Query Parameters (Optional):

| Param | Type | Description | Example |
|-------|------|-------------|---------|
| `page` | Number | Trang hiện tại | `?page=1` |
| `limit` | Number | Số items/trang | `?limit=10` |
| `status` | String | Filter theo status | `?status=pending` |

### Examples:

**Lấy tất cả:**
```
GET http://localhost:5000/api/orders
```

**Lấy trang 2, mỗi trang 5 items:**
```
GET http://localhost:5000/api/orders?page=2&limit=5
```

**Lấy orders đang pending:**
```
GET http://localhost:5000/api/orders?status=pending
```

### Expected Response (200 OK):
```json
{
  "data": [
    {
      "id": 1,
      "user_id": 2,
      "table_number": 5,
      "status": "pending",
      "note": "Không đường, ít đá",
      "total_amount": "125000.00",
      "created_at": "2025-12-12T09:00:00.000Z",
      "updated_at": "2025-12-12T09:00:00.000Z",
      "items": [
        {
          "id": 1,
          "order_id": 1,
          "product_id": 1,
          "quantity": 2,
          "unit_price": "45000.00",
          "subtotal": "90000.00"
        }
      ]
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 10,
    "total": 1,
    "pages": 1
  }
}
```

### 🔒 Phân Quyền:
- **Customer:** Chỉ thấy orders của chính mình
- **Admin:** Thấy tất cả orders của mọi người

---

## 3️⃣ GET - Lấy Order Chi Tiết

### Request

**Method:** `GET`  
**URL:** `http://localhost:5000/api/orders/1`  
**Headers:**
- `Authorization: Bearer YOUR_TOKEN`

### Expected Response (200 OK):
```json
{
  "id": 1,
  "user_id": 2,
  "table_number": 5,
  "status": "pending",
  "note": "Không đường, ít đá",
  "total_amount": "125000.00",
  "created_at": "2025-12-12T09:00:00.000Z",
  "updated_at": "2025-12-12T09:00:00.000Z",
  "items": [
    {
      "id": 1,
      "order_id": 1,
      "product_id": 1,
      "quantity": 2,
      "unit_price": "45000.00",
      "subtotal": "90000.00"
    },
    {
      "id": 2,
      "order_id": 1,
      "product_id": 2,
      "quantity": 1,
      "unit_price": "35000.00",
      "subtotal": "35000.00"
    }
  ]
}
```

---

## 4️⃣ PUT - Update Order Status (Admin Only)

### Request

**Method:** `PUT`  
**URL:** `http://localhost:5000/api/orders/1/status`  
**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer ADMIN_TOKEN` (⚠️ Cần admin token)

**Body (JSON):**
```json
{
  "status": "confirmed"
}
```

### Valid Status Values:
- `pending` - Chờ xác nhận
- `confirmed` - Đã xác nhận
- `preparing` - Đang chuẩn bị
- `ready` - Sẵn sàng
- `delivered` - Đã giao
- `cancelled` - Đã hủy

### Expected Response (200 OK):
```json
{
  "id": 1,
  "user_id": 2,
  "table_number": 5,
  "status": "confirmed",
  "note": "Không đường, ít đá",
  "total_amount": "125000.00",
  "created_at": "2025-12-12T09:00:00.000Z",
  "updated_at": "2025-12-12T09:05:00.000Z"
}
```

### 💡 Test Workflow:
```
pending → confirmed → preparing → ready → delivered
```

---

# 💳 PAYMENT API

## 1️⃣ POST - Tạo Payment

### Request

**Method:** `POST`  
**URL:** `http://localhost:5000/api/payments`  
**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer YOUR_TOKEN`

**Body (JSON):**
```json
{
  "order_id": 1,
  "amount": 125000,
  "method": "cash",
  "status": "completed"
}
```

### Fields:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `order_id` | Number | ✅ | ID của order cần thanh toán |
| `amount` | Number | ✅ | Số tiền thanh toán |
| `method` | String | ❌ | Phương thức: `cash`, `momo`, `vnpay`, `card` (default: `cash`) |
| `status` | String | ❌ | Trạng thái: `pending`, `completed`, `success`, `failed` (default: `pending`) |
| `transaction_id` | String | ❌ | Mã giao dịch (cho online payment) |

### Expected Response (201 Created):
```json
{
  "id": 1,
  "order_id": 1,
  "amount": "125000.00",
  "method": "cash",
  "status": "completed",
  "transaction_id": null,
  "created_at": "2025-12-12T09:10:00.000Z",
  "updated_at": "2025-12-12T09:10:00.000Z"
}
```

### 💡 Test Cases:

**Cash Payment:**
```json
{
  "order_id": 1,
  "amount": 125000,
  "method": "cash",
  "status": "completed"
}
```

**MoMo Payment:**
```json
{
  "order_id": 2,
  "amount": 200000,
  "method": "momo",
  "status": "pending",
  "transaction_id": "MOMO123456789"
}
```

**VNPay Payment:**
```json
{
  "order_id": 3,
  "amount": 150000,
  "method": "vnpay",
  "status": "success",
  "transaction_id": "VNP20251212001"
}
```

---

## 2️⃣ GET - Lấy Danh Sách Payments (Admin Only)

### Request

**Method:** `GET`  
**URL:** `http://localhost:5000/api/payments`  
**Headers:**
- `Authorization: Bearer ADMIN_TOKEN` (⚠️ Chỉ admin)

### Query Parameters:

| Param | Description | Example |
|-------|-------------|---------|
| `order_id` | Filter theo order | `?order_id=1` |

### Examples:

**Lấy tất cả payments:**
```
GET http://localhost:5000/api/payments
```

**Lấy payments của order cụ thể:**
```
GET http://localhost:5000/api/payments?order_id=1
```

### Expected Response (200 OK):
```json
[
  {
    "id": 1,
    "order_id": 1,
    "amount": "125000.00",
    "method": "cash",
    "status": "completed",
    "transaction_id": null,
    "created_at": "2025-12-12T09:10:00.000Z",
    "updated_at": "2025-12-12T09:10:00.000Z"
  },
  {
    "id": 2,
    "order_id": 2,
    "amount": "200000.00",
    "method": "momo",
    "status": "pending",
    "transaction_id": "MOMO123456789",
    "created_at": "2025-12-12T09:15:00.000Z",
    "updated_at": "2025-12-12T09:15:00.000Z"
  }
]
```

---

## 3️⃣ GET - Lấy Payment Chi Tiết

### Request

**Method:** `GET`  
**URL:** `http://localhost:5000/api/payments/1`  
**Headers:**
- `Authorization: Bearer YOUR_TOKEN`

### Expected Response (200 OK):
```json
{
  "id": 1,
  "order_id": 1,
  "amount": "125000.00",
  "method": "cash",
  "status": "completed",
  "transaction_id": null,
  "created_at": "2025-12-12T09:10:00.000Z",
  "updated_at": "2025-12-12T09:10:00.000Z"
}
```

---

## 4️⃣ PUT - Update Payment

### Request

**Method:** `PUT`  
**URL:** `http://localhost:5000/api/payments/1`  
**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer YOUR_TOKEN`

**Body (JSON):**
```json
{
  "status": "completed",
  "transaction_id": "CASH123456"
}
```

### Expected Response (200 OK):
```json
{
  "id": 1,
  "order_id": 1,
  "amount": "125000.00",
  "method": "cash",
  "status": "completed",
  "transaction_id": "CASH123456",
  "created_at": "2025-12-12T09:10:00.000Z",
  "updated_at": "2025-12-12T09:20:00.000Z"
}
```

### 💡 Use Cases:

**Update MoMo payment từ pending → success:**
```json
{
  "status": "success",
  "transaction_id": "MOMO987654321"
}
```

**Update failed payment:**
```json
{
  "status": "failed"
}
```

---

## 5️⃣ DELETE - Xóa Payment (Admin Only)

### Request

**Method:** `DELETE`  
**URL:** `http://localhost:5000/api/payments/1`  
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

### Scenario: Customer đặt hàng và thanh toán

```
1. Login as Customer
   POST /api/users/login
   → Lấy token

2. Tạo Order
   POST /api/orders
   Body: { table_number: 5, items: [...] }
   → Nhận order_id = 1, total_amount = 125000

3. Xem Order vừa tạo
   GET /api/orders/1
   → Verify order details

4. Tạo Payment
   POST /api/payments
   Body: { order_id: 1, amount: 125000, method: "cash" }
   → Nhận payment_id = 1

5. Xem Payment
   GET /api/payments/1
   → Verify payment details

6. Update Payment status
   PUT /api/payments/1
   Body: { status: "completed" }
   → Payment hoàn tất
```

### Scenario: Admin quản lý orders

```
1. Login as Admin
   POST /api/users/login (admin account)
   → Lấy admin token

2. Xem tất cả Orders
   GET /api/orders
   → Thấy orders của tất cả customers

3. Update Order Status
   PUT /api/orders/1/status
   Body: { status: "confirmed" }
   → Order chuyển sang confirmed

4. Update tiếp
   PUT /api/orders/1/status
   Body: { status: "preparing" }
   → Order đang chuẩn bị

5. Xem tất cả Payments
   GET /api/payments
   → Thấy tất cả payments

6. Xóa Payment (nếu cần)
   DELETE /api/payments/1
   → Xóa thành công
```

---

## 📋 Quick Reference

### Order API

| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/api/orders` | ✅ | Customer/Admin | Tạo order |
| GET | `/api/orders` | ✅ | Customer/Admin | List orders |
| GET | `/api/orders/:id` | ✅ | Customer/Admin | Get order |
| PUT | `/api/orders/:id/status` | ✅ | Admin | Update status |

### Payment API

| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/api/payments` | ✅ | Customer | Tạo payment |
| GET | `/api/payments` | ✅ | Admin | List payments |
| GET | `/api/payments/:id` | ✅ | Customer/Admin | Get payment |
| PUT | `/api/payments/:id` | ✅ | Customer/Admin | Update payment |
| DELETE | `/api/payments/:id` | ✅ | Admin | Delete payment |

---

## ✅ Test Checklist

### Order API
- [ ] POST Create Order (customer token) → 201
- [ ] GET All Orders (customer token) → 200, chỉ thấy orders của mình
- [ ] GET All Orders (admin token) → 200, thấy tất cả
- [ ] GET Single Order → 200
- [ ] PUT Update Status (admin token) → 200
- [ ] PUT Update Status (customer token) → 403 Forbidden

### Payment API
- [ ] POST Create Payment (cash) → 201
- [ ] POST Create Payment (momo) → 201
- [ ] GET All Payments (admin token) → 200
- [ ] GET All Payments (customer token) → 403 Forbidden
- [ ] GET Single Payment → 200
- [ ] PUT Update Payment status → 200
- [ ] DELETE Payment (admin token) → 200
- [ ] DELETE Payment (customer token) → 403 Forbidden

---

## 🐛 Common Errors

### 400 Bad Request
```json
{ "message": "Items array is required" }
```
**Fix:** Thêm `items` array vào body

### 401 Unauthorized
```json
{ "message": "No token provided" }
```
**Fix:** Thêm `Authorization: Bearer TOKEN` header

### 403 Forbidden
```json
{ "message": "Access denied" }
```
**Fix:** Dùng đúng role (admin/customer)

### 404 Not Found
```json
{ "message": "Order not found" }
```
**Fix:** Kiểm tra order_id có tồn tại không

---

**Chúc bạn test thành công! 🚀**
