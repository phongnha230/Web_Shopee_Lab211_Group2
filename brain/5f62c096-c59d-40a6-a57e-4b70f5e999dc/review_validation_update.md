# ✅ Đã Cập Nhật: Review Validation - Payment Check

## 🎯 Thay Đổi Mới

**File:** [`reviewController.js`](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/backend/controllers/reviewController.js)

### **Validation Mới (Đầy Đủ):**

```javascript
// ✅ CHECK 1: User đã mua product và order đã delivered?
const purchasedOrder = await Order.findOne({
  where: {
    user_id: userId,
    status: 'delivered'
  },
  include: [{
    model: OrderItem,
    as: 'items',
    where: { product_id },
    required: true
  }]
});

if (!purchasedOrder) {
  return res.status(403).json({ 
    message: 'Bạn chỉ có thể đánh giá sản phẩm sau khi đã mua và nhận hàng thành công.' 
  });
}

// ✅ CHECK 2: Order đã thanh toán chưa?
const payment = await Payment.findOne({
  where: {
    order_id: purchasedOrder.id
  }
});

if (!payment || (payment.status !== 'completed' && payment.status !== 'success')) {
  return res.status(403).json({ 
    message: 'Bạn cần thanh toán đơn hàng trước khi đánh giá sản phẩm.' 
  });
}
```

---

## 📋 Điều Kiện Để Review (ĐẦY ĐỦ)

1. ✅ **Đăng nhập** (có user_id)
2. ✅ **Đã mua product** (có order chứa product_id)
3. ✅ **Order status = 'delivered'** (đã giao hàng)
4. ✅ **Payment status = 'completed' hoặc 'success'** (đã thanh toán)
5. ✅ **Chưa review product này** (không trùng)

---

## 🧪 Test Với Dữ Liệu Thực

### **Từ Database Screenshots:**

**Orders đã delivered:**
- Order ID: 4, 5, 6, 7, 8, 9
- User ID: 15
- Status: delivered ✅

**Payments:**
- Cần check xem orders này đã có payment completed chưa

---

### **Scenario 1: Review Thành Công ✅**

**Điều kiện:**
- Order ID 4: user_id=15, status='delivered', product_id=15
- Payment: order_id=4, status='completed'

**Test:**
```
1. Login với user_id = 15
   POST /api/users/login

2. POST Review
   POST /api/reviews
   Authorization: Bearer TOKEN
   Body: {
     "product_id": 15,
     "rating": 5,
     "comment": "Rất ngon!"
   }

Expected: 201 Created ✅
```

---

### **Scenario 2: Order Delivered Nhưng Chưa Thanh Toán ❌**

**Điều kiện:**
- Order ID 12: user_id=15, status='confirmed', product_id=16
- Payment: KHÔNG CÓ hoặc status='pending'

**Test:**
```
POST /api/reviews
Body: {
  "product_id": 16,
  "rating": 5
}

Expected: 403 Forbidden ❌
{
  "message": "Bạn cần thanh toán đơn hàng trước khi đánh giá sản phẩm."
}
```

---

### **Scenario 3: Chưa Mua Product ❌**

**Test:**
```
POST /api/reviews
Body: {
  "product_id": 99,  // Chưa từng mua
  "rating": 5
}

Expected: 403 Forbidden ❌
{
  "message": "Bạn chỉ có thể đánh giá sản phẩm sau khi đã mua và nhận hàng thành công."
}
```

---

## 🔄 Complete Workflow

```
1. Customer đặt hàng
   POST /api/orders
   → Order status = "pending"

2. Customer thanh toán
   POST /api/payments
   Body: {
     "order_id": 12,
     "amount": 60000,
     "method": "vnpay",
     "status": "pending"
   }

3. Payment gateway xử lý
   → Payment status = "completed" ✅

4. Admin giao hàng
   PUT /api/orders/12/status
   Body: {"status": "delivered"} ✅

5. BÂY GIỜ customer mới review được
   POST /api/reviews
   → 201 Created ✅
```

---

## 📊 Validation Flow

```
User POST /api/reviews
    ↓
1. Đã login? → NO → 401 Unauthorized
    ↓ YES
2. Đã mua product? → NO → 403 Forbidden
    ↓ YES
3. Order delivered? → NO → 403 Forbidden
    ↓ YES
4. Payment completed? → NO → 403 Forbidden ("Bạn cần thanh toán...")
    ↓ YES
5. Đã review rồi? → YES → 400 Bad Request
    ↓ NO
6. Tạo review → 201 Created ✅
```

---

## 💡 Payment Status Values

Từ `payment.js` model:
```javascript
status: ENUM('pending', 'completed', 'success', 'failed')
```

**Validation chấp nhận:**
- ✅ `completed`
- ✅ `success`
- ❌ `pending` → Chưa thanh toán
- ❌ `failed` → Thanh toán thất bại

---

## 🧪 Test Checklist

- [ ] Review khi chưa đăng nhập → 401
- [ ] Review khi chưa mua → 403 "chưa mua và nhận hàng"
- [ ] Review khi order pending → 403 "chưa mua và nhận hàng"
- [ ] Review khi order delivered nhưng chưa payment → 403 "cần thanh toán"
- [ ] Review khi order delivered + payment pending → 403 "cần thanh toán"
- [ ] Review khi order delivered + payment failed → 403 "cần thanh toán"
- [ ] Review khi order delivered + payment completed → 201 Created ✅
- [ ] Review trùng → 400 Bad Request

---

## 🎯 Kết Luận

**Bây giờ validation HOÀN HẢO:**
- ✅ Phải mua hàng
- ✅ Phải thanh toán
- ✅ Phải nhận hàng
- ✅ Mới được review

**Đúng như yêu cầu của bạn!** 🎉
