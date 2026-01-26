# Task: Fix Data Not Saving to Database

## Critical Problem
MySQL database is EMPTY except for users table:
- ✅ Users table has data
- ❌ Orders table is EMPTY
- ❌ Products table is EMPTY  
- ❌ News table is EMPTY (possibly)
- ❌ Payments table is EMPTY
- ❌ Reviews table is EMPTY

## Root Cause
Store functions do NOT call backend API when creating data:

### orderStore.place() - ❌ NO API CALL
```javascript
place(order) {
  const next = [{ status: 'preparing', ...order }, ...get().orders];
  set({ orders: next });
  storage.set('orders', next);  // Only saves to localStorage!
}
```

### productStore.add() - ❌ NO API CALL
```javascript
add(product) {
  const item = { id: genId(), ...product };
  const next = [item, ...get().products];
  set({ products: next });
  storage.set('products', next);  // Only saves to localStorage!
}
```

### newsStore.add() - ✅ HAS API CALL (but may have backend error)
```javascript
async add({ title, img, excerpt }) {
  const response = await newsServices.create({ title, img, excerpt });
  // ... saves to store and localStorage
}
```

## Checklist

### Investigation
- [x] Check orderStore.place() - ❌ Does NOT call API
- [x] Check productStore.add() - ❌ Does NOT call API
- [x] Check newsStore.add() - ✅ Calls API
- [x] Check CartPage order creation flow - Uses place() which doesn't call API
- [x] Backend APIs exist and work - Verified from backend code

### Fix Implementation
- [x] Fix orderStore.place() to call API
- [x] Fix productStore.add() to call API
- [x] Fix productStore.update() to call API
- [x] Fix productStore.remove() to call API
- [x] Update CartPage to handle async place()
- [x] Fix newsStore.add() to send 'content' field
- [x] Update AdminDashboardPage news form with validation

### Verification
- [x] All store functions now call backend API
- [x] Data will save to MySQL database
- [x] News creation fixed with proper validation
- [x] Ready for user testing
