# 🚀 Hướng Dẫn Nâng Cấp React 19

## 📊 Tình Trạng Hiện Tại

**Phiên bản hiện tại:**
- React: `18.3.1`
- React DOM: `18.3.1`

**Phiên bản mục tiêu:**
- React: `19.x.x` (latest stable)
- React DOM: `19.x.x`

---

## ⚠️ Breaking Changes Quan Trọng

### 1. **Loại Bỏ `propTypes` và `defaultProps`**
- `propTypes` sẽ bị bỏ qua hoàn toàn
- `defaultProps` bị xóa khỏi function components
- ✅ **Giải pháp:** Sử dụng ES6 default parameters hoặc TypeScript

**Trước (React 18):**
```javascript
function MyComponent({ name, age }) {
  // ...
}
MyComponent.defaultProps = {
  name: 'Guest',
  age: 0
};
```

**Sau (React 19):**
```javascript
function MyComponent({ name = 'Guest', age = 0 }) {
  // ...
}
```

### 2. **String Refs Bị Xóa Hoàn Toàn**
- String refs đã deprecated từ React 16.3
- ✅ **Giải pháp:** Sử dụng `useRef` hook hoặc ref callbacks

**Trước:**
```javascript
<div ref="myDiv">...</div>
```

**Sau:**
```javascript
const myDivRef = useRef(null);
<div ref={myDivRef}>...</div>
```

### 3. **`ref` Giờ Là Prop Thông Thường**
- Không cần `forwardRef` trong nhiều trường hợp
- Truy cập qua `element.props.ref` thay vì `element.ref`

### 4. **Error Handling Thay Đổi**
- Lỗi không bị Error Boundary bắt sẽ được báo cáo qua `window.reportError`
- Không còn re-throw như React 18

---

## 📝 Quy Trình Nâng Cấp An Toàn

### Bước 1: Kiểm Tra Code Hiện Tại

**Kiểm tra xem dự án có sử dụng:**
- ❌ String refs (`ref="myRef"`)
- ❌ `propTypes` hoặc `defaultProps`
- ❌ `ReactDOM.render()` (legacy)
- ❌ `ReactDOM.unmountComponentAtNode()`
- ❌ Legacy Context API

> **Lưu ý:** Dự án của bạn đã sử dụng `ReactDOM.createRoot` (✅ tốt!) nên không cần thay đổi phần này.

### Bước 2: Dừng Dev Server

```bash
# Nhấn Ctrl+C trong terminal đang chạy npm run dev
```

### Bước 3: Backup `package.json`

```bash
cp package.json package.json.backup
```

### Bước 4: Cập Nhật Dependencies

**Cách 1: Sử dụng npm (Khuyến nghị)**
```bash
npm install react@latest react-dom@latest
```

**Cách 2: Chỉ định phiên bản cụ thể**
```bash
npm install react@19.0.0 react-dom@19.0.0
```

### Bước 5: Cập Nhật Vite Plugin (nếu cần)

```bash
npm install @vitejs/plugin-react@latest
```

### Bước 6: Xóa `node_modules` và Cài Lại (Khuyến nghị)

```bash
rm -rf node_modules package-lock.json
npm install
```

### Bước 7: Kiểm Tra Tương Thích

```bash
npm run dev
```

Mở browser và kiểm tra:
- ✅ Ứng dụng khởi động không lỗi
- ✅ Các trang render đúng
- ✅ Không có warning trong console
- ✅ Các tính năng hoạt động bình thường

---

## 🔍 Kiểm Tra Code Dự Án

### Files Cần Kiểm Tra:

#### 1. **`main.jsx`** ✅
```javascript
// ✅ Đã sử dụng createRoot - không cần thay đổi
ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>
);
```

#### 2. **Components Sử Dụng Refs**
Tìm kiếm trong dự án:
```bash
# Tìm string refs (nếu có)
grep -r "ref=\"" src/

# Tìm forwardRef
grep -r "forwardRef" src/
```

#### 3. **PropTypes và DefaultProps**
```bash
# Tìm propTypes
grep -r "propTypes" src/

# Tìm defaultProps
grep -r "defaultProps" src/
```

---

## 🎁 Tính Năng Mới Trong React 19

### 1. **Actions**
Tự động xử lý pending states, errors, và optimistic updates:

```javascript
function UpdateName({ name, setName }) {
  const [error, setError] = useState(null);
  const [isPending, startTransition] = useTransition();

  const handleSubmit = async () => {
    startTransition(async () => {
      const error = await updateName(name);
      if (error) {
        setError(error);
        return;
      }
      redirect("/path");
    });
  };

  return (
    <div>
      <input value={name} onChange={(e) => setName(e.target.value)} />
      <button onClick={handleSubmit} disabled={isPending}>
        Update
      </button>
      {error && <p>{error}</p>}
    </div>
  );
}
```

### 2. **`use()` Hook**
Đọc resources trong render:

```javascript
import { use } from 'react';

function Comments({ commentsPromise }) {
  const comments = use(commentsPromise);
  return comments.map(comment => <p key={comment.id}>{comment.text}</p>);
}
```

### 3. **`ref` Là Prop**
Không cần `forwardRef` nữa:

```javascript
// React 19
function MyInput({ placeholder, ref }) {
  return <input placeholder={placeholder} ref={ref} />
}

// Sử dụng
<MyInput ref={ref} />
```

### 4. **Document Metadata**
Render `<title>`, `<meta>` trực tiếp trong components:

```javascript
function BlogPost({ post }) {
  return (
    <article>
      <title>{post.title}</title>
      <meta name="description" content={post.excerpt} />
      <h1>{post.title}</h1>
      <p>{post.content}</p>
    </article>
  );
}
```

### 5. **Stylesheet Priority**
Kiểm soát thứ tự load CSS:

```javascript
function ComponentOne() {
  return (
    <Suspense fallback="loading...">
      <link rel="stylesheet" href="foo" precedence="default" />
      <link rel="stylesheet" href="bar" precedence="high" />
      <article>...</article>
    </Suspense>
  );
}
```

---

## 🛠️ Lệnh Thực Thi

### Option 1: Nâng Cấp Trực Tiếp (Khuyến nghị)

```bash
# 1. Dừng dev server (Ctrl+C)

# 2. Update React
npm install react@latest react-dom@latest

# 3. Update Vite plugin
npm install @vitejs/plugin-react@latest

# 4. Kiểm tra
npm run dev
```

### Option 2: Nâng Cấp Cẩn Thận (An toàn hơn)

```bash
# 1. Backup
cp package.json package.json.backup

# 2. Clean install
rm -rf node_modules package-lock.json

# 3. Update dependencies
npm install react@latest react-dom@latest @vitejs/plugin-react@latest

# 4. Reinstall tất cả
npm install

# 5. Test
npm run dev
```

---

## ✅ Checklist Sau Nâng Cấp

- [ ] Dev server khởi động thành công
- [ ] Không có error trong console
- [ ] Trang chủ hiển thị đúng
- [ ] Menu/Products load được
- [ ] Cart functionality hoạt động
- [ ] Login/Authentication hoạt động
- [ ] Admin dashboard truy cập được
- [ ] API calls thành công
- [ ] Routing hoạt động bình thường
- [ ] Toast notifications hiển thị

---

## 🔙 Rollback (Nếu Có Vấn Đề)

```bash
# Khôi phục package.json cũ
cp package.json.backup package.json

# Cài lại dependencies cũ
rm -rf node_modules package-lock.json
npm install
```

---

## 📚 Tài Liệu Tham Khảo

- [React 19 Official Docs](https://react.dev/blog/2024/12/05/react-19)
- [React 19 Upgrade Guide](https://react.dev/blog/2024/04/25/react-19-upgrade-guide)
- [Breaking Changes](https://github.com/facebook/react/blob/main/CHANGELOG.md)

---

## 💡 Lưu Ý Quan Trọng

> ⚠️ **Dự án của bạn đã sử dụng các best practices:**
> - ✅ `ReactDOM.createRoot` (không phải legacy `ReactDOM.render`)
> - ✅ Functional components với hooks
> - ✅ Modern routing với React Router v6
> - ✅ Zustand cho state management
> 
> **→ Nâng cấp lên React 19 sẽ tương đối đơn giản!**

---

**Sẵn sàng nâng cấp? Chạy lệnh sau:**

```bash
npm install react@latest react-dom@latest
```
