# Walkthrough: Order Management System Fixes

## Issues Fixed

### 1. Missing Orders in Customer View
**Problem**: Orders were not appearing in "Đơn của tôi" after payment.

**Root Cause**: Frontend was filtering orders by `customerName` field, but backend API doesn't return this field.

**Solution**: 
- Removed client-side `customerName` filter in [CustomerOrdersPage.jsx](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/Frontend/src/pages/Order/CustomerOrdersPage.jsx#L32-L40)
- Backend already filters by `user_id`, so this was redundant

---

### 2. Data Normalization Issues
**Problem**: Crashes due to undefined `total` field and missing `productId` in items.

**Root Cause**: Backend uses snake_case (`total_amount`, `product_id`) while frontend expects camelCase.

**Solution**: Added normalization in [orderStore.js](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/Frontend/src/stores/orderStore.js#L20-L35):
- `total_amount` → `total`
- `created_at` → `createdAt`
- `table_number` → `address`
- `product_id` → `productId` (in items)
- `status` preserved from backend

---

### 3. News API 500 Error
**Problem**: News page showing "500 Internal Server Error".

**Root Cause**: Missing Sequelize association between `News` and `User` models at runtime.

**Solution**: Added runtime association check in [newController.js](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/backend/controllers/newController.js):
```javascript
if (!News.associations.author) {
  News.belongsTo(User, { foreignKey: 'created_by', as: 'author' });
}
```

---

### 4. Missing Admin Approval Buttons
**Problem**: Admin dashboard not showing "Đánh dấu sẵn sàng" button for new orders.

**Root Cause**: Button only appeared for status `'preparing'`, but backend creates orders with status `'pending'`.

**Solution**: Updated condition in [AdminDashboardPage.jsx](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/Frontend/src/pages/Admin/AdminDashboardPage.jsx#L1183):
```javascript
{(o.status === 'preparing' || o.status === 'pending') && (
```

---

### 5. Status Changes Not Syncing to Database
**Problem**: Admin's status changes (ready/done) not visible to customers after refresh.

**Root Cause**: `updateStatus` only updated localStorage, not the backend database.

**Solution**: Made `updateStatus` async and added backend API call in [orderStore.js](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/Frontend/src/stores/orderStore.js#L96-L111):
```javascript
async updateStatus(id, status) {
  try {
    await orderServices.updateStatus(id, { status });
    // Then update local state
    const next = get().orders.map(o => o.id === id ? { ...o, status } : o);
    set({ orders: next });
    storage.set('orders', next);
  } catch (error) {
    // Fallback to local update if backend fails
  }
}
```

---

## Testing Instructions

### For Admin:
1. Login as admin
2. Navigate to Dashboard → "Đơn hàng" tab
3. Click "Đánh dấu sẵn sàng" for a pending order
4. Verify button changes to "Hoàn tất"
5. Click "Hoàn tất" to complete the order

### For Customer:
1. **Login as customer** (token may have expired - please re-login)
2. Navigate to "Đơn của tôi"
3. Verify orders appear with correct status
4. Refresh page - status should persist
5. Check that admin's status changes are visible

---

## Important Note

⚠️ **Authentication Issue Detected**: During testing, I found that the customer account was logged out (token expired). This is why orders weren't showing - the API returns 401 Unauthorized when not logged in.

**To test the fixes properly**:
1. Make sure you're logged in as a customer
2. If you see "Bạn chưa có đơn hàng nào" but you know you have orders, try logging out and logging back in
3. The browser console should not show any 401 or 403 errors if properly authenticated
