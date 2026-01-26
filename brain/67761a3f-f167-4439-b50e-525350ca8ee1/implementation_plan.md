# Fix Critical Issue: Data Not Saving to MySQL Database

## Problem

User reports that MySQL database is **COMPLETELY EMPTY** except for users table:
- ✅ **Users table**: Has data (because register/login call API)
- ❌ **Orders table**: EMPTY
- ❌ **Products table**: EMPTY
- ❌ **News table**: EMPTY (possibly)
- ❌ **Payments table**: EMPTY
- ❌ **Reviews table**: EMPTY

All data only exists in browser localStorage and disappears when localStorage is cleared.

## Root Cause

Store functions do NOT call backend API when creating/updating/deleting data:

### 1. orderStore.place() - ❌ NO API CALL

**Current code** ([orderStore.js:37-41](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/Frontend/src/stores/orderStore.js#L37-L41)):
```javascript
place(order) {
  const next = [{ status: 'preparing', ...order }, ...get().orders];
  set({ orders: next });
  storage.set('orders', next);  // ❌ Only saves to localStorage!
}
```

**Problem**: When user creates order in CartPage, it calls `place(order)` which only saves to localStorage, never sends to backend.

### 2. productStore.add/update/remove() - ❌ NO API CALLS

**Current code** ([productStore.js:52-68](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/Frontend/src/stores/productStore.js#L52-L68)):
```javascript
add(product) {
  const item = { id: genId(), ...product };
  const next = [item, ...get().products];
  set({ products: next });
  storage.set('products', next);  // ❌ Only saves to localStorage!
}

update(id, patch) {
  const next = get().products.map(p => p.id === id ? { ...p, ...patch } : p);
  set({ products: next });
  storage.set('products', next);  // ❌ Only saves to localStorage!
}

remove(id) {
  const next = get().products.filter(p => p.id !== id);
  set({ products: next });
  storage.set('products', next);  // ❌ Only saves to localStorage!
}
```

**Problem**: When admin adds/edits/deletes products, changes only go to localStorage.

### 3. newsStore.add() - ✅ HAS API CALL (but may have backend error)

**Current code** ([newsStore.js:40-69](file:///c:/Users/HPPAVILION/Documents/Cusor/Cafe_app/my-app/Frontend/src/stores/newsStore.js#L40-L69)):
```javascript
async add({ title, img, excerpt }) {
  const response = await newsServices.create({ title, img, excerpt });
  // ... saves to store
}
```

**Status**: Code looks correct, but if news table is empty, backend may have validation error.

## Proposed Changes

### 1. Fix orderStore.place()

Rewrite to call backend API:

```javascript
async place(order) {
  set({ loading: true, error: null });
  try {
    // Call backend API to create order
    const response = await orderServices.create({
      table_number: order.address,  // Backend expects table_number
      note: order.note || null,
      items: order.items.map(item => ({
        product_id: item.productId,
        quantity: item.quantity,
        unit_price: item.product?.price || 0,  // Need to get price from product
      })),
    });

    const createdOrder = response.data.order;
    
    // Add to local store
    const next = [createdOrder, ...get().orders];
    set({ orders: next, loading: false });
    storage.set('orders', next);
    
    return createdOrder.id;
  } catch (error) {
    console.error('Create order failed:', error);
    set({ error: error.message, loading: false });
    throw error;
  }
}
```

### 2. Fix productStore.add()

Rewrite to call backend API:

```javascript
async add(product) {
  set({ loading: true, error: null });
  try {
    const response = await productServices.create({
      name: product.name,
      price: product.price,
      category: product.category,
      image_url: product.image,  // Backend uses image_url
      description: product.description || null,
    });

    const createdProduct = response.data;
    
    // Format for frontend
    const formatted = {
      id: createdProduct.id,
      name: createdProduct.name,
      price: parseFloat(createdProduct.price),
      category: createdProduct.category,
      image: createdProduct.image_url,
    };

    const next = [formatted, ...get().products];
    set({ products: next, loading: false });
    storage.set('products', next);
    
    return formatted.id;
  } catch (error) {
    console.error('Add product failed:', error);
    set({ error: error.message, loading: false });
    throw error;
  }
}
```

### 3. Fix productStore.update()

```javascript
async update(id, patch) {
  set({ loading: true, error: null });
  try {
    await productServices.update(id, {
      name: patch.name,
      price: patch.price,
      category: patch.category,
      image_url: patch.image,
    });

    const next = get().products.map(p => p.id === id ? { ...p, ...patch } : p);
    set({ products: next, loading: false });
    storage.set('products', next);
  } catch (error) {
    console.error('Update product failed:', error);
    set({ error: error.message, loading: false });
    throw error;
  }
}
```

### 4. Fix productStore.remove()

```javascript
async remove(id) {
  set({ loading: true, error: null });
  try {
    await productServices.delete(id);

    const next = get().products.filter(p => p.id !== id);
    set({ products: next, loading: false });
    storage.set('products', next);
  } catch (error) {
    console.error('Remove product failed:', error);
    set({ error: error.message, loading: false });
    throw error;
  }
}
```

### 5. Update CartPage to handle async place()

Since `place()` will now be async, CartPage needs to await it:

```javascript
// In confirmPayment function
try {
  await place(order);  // Now async
  // ... rest of code
} catch (error) {
  console.error('Order creation failed:', error);
  alert('Đặt hàng thất bại: ' + error.message);
}
```

## Important Notes

### Backend API Requirements

Need to verify backend APIs exist and accept correct data format:

1. **POST /api/orders** - Create order
   - Expects: `{ table_number, note, items: [{ product_id, quantity, unit_price }] }`
   
2. **POST /api/products** - Create product
   - Expects: `{ name, price, category, image_url, description }`
   
3. **PUT /api/products/:id** - Update product
   
4. **DELETE /api/products/:id** - Delete product

### Breaking Changes

⚠️ **WARNING**: These changes make store functions async. Any code calling these functions must be updated to use `await`:

**Before:**
```javascript
place(order);  // Synchronous
add(product);  // Synchronous
```

**After:**
```javascript
await place(order);  // Async
await add(product);  // Async
```

## Verification Plan

### Step 1: Check Backend APIs

Test backend endpoints directly:

```bash
# Test create order
curl -X POST http://localhost:5000/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"table_number":"Bàn 1","items":[{"product_id":1,"quantity":2,"unit_price":30000}]}'

# Test create product
curl -X POST http://localhost:5000/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"name":"Test Product","price":50000,"category":"coffee","image_url":"https://example.com/image.jpg"}'
```

### Step 2: Implement Changes

1. Update `orderStore.js`
2. Update `productStore.js`
3. Update `CartPage.jsx` to handle async
4. Update `AdminDashboardPage.jsx` to handle async

### Step 3: Test Frontend

1. **Test Order Creation**:
   - Add items to cart
   - Checkout with cash payment
   - Verify order appears in admin dashboard
   - Check MySQL: `SELECT * FROM orders;`

2. **Test Product Creation**:
   - Login as admin
   - Add new product
   - Verify product appears in menu
   - Check MySQL: `SELECT * FROM products;`

3. **Test Product Update/Delete**:
   - Edit product name/price
   - Delete product
   - Check MySQL for changes

### Step 4: Verify Data Persistence

1. Create test data (orders, products, news)
2. Restart backend server
3. Clear localStorage
4. Refresh frontend
5. Verify all data loads from MySQL

## Expected Results

After fixes:
- ✅ Orders save to MySQL when created
- ✅ Products save to MySQL when added/edited/deleted
- ✅ Data persists in database across server restarts
- ✅ Data persists even when localStorage is cleared
- ✅ Admin dashboard shows real data from database
- ✅ No more "data lost" issues
