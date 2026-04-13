# 📓 Log Làm Việc Với Antigravity (Web Shopee Clone)

> Cập nhật lần cuối: **2026-03-04 13:08 GMT+7**
> Repository: `phongnha230/Web_Shopee`

---

## 📅 Session 1 — 2026-03-03 (Khoảng 09:57 – 10:05 GMT+7)

### 🔧 Fixing Dashboard Functionality

**Conversation ID:** `3fe4bd57-b0e4-4064-8136-4949a51d72ec`

**❓ Prompt của user:**

> Fix các nút "24h" và "7N" trên dashboard không hoạt động, và cải thiện tính năng "stock alert" để hữu ích hơn.

**✅ Câu trả lời / Việc đã làm:**

- Fix logic lọc dữ liệu theo khoảng thời gian cho nút **24h** và **7N** trên Admin Dashboard
- Cải thiện tính năng **stock alert** để hiển thị thông tin stock hữu ích hơn

**📁 Files bị ảnh hưởng:**

- `src/Frontend/admin-dashboard.html` — Fix filter logic + stock alert UI

---

## 📅 Session 2 — 2026-03-03 (Khoảng 10:12 – 10:15 GMT+7)

### 🔧 Fixing Write Review Link (lần 1)

**Conversation ID:** `61ad35e7-7b3b-4f63-95c5-feafed5fb3fd`

**❓ Prompt của user:**

> Fix link bị hỏng từ trang "My Orders" sang trang "Write Review". Vấn đề là ID đơn hàng sai vì xử lý sai composite key.

**✅ Câu trả lời / Việc đã làm:**

- Xác định lỗi: chuỗi composite key bị cắt sai → truyền sai `orderId` sang trang write-review
- Fix cách lấy `orderId` đúng từ composite key

**📁 Files bị ảnh hưởng:**

- `src/Frontend/` (trang my-orders)

---

## 📅 Session 3 — 2026-03-03 (Khoảng 10:19 – 10:21 GMT+7)

### 🔧 Fixing Write Review Link (lần 2)

**Conversation ID:** `7bf7245f-cbd0-47c0-b595-f971e30f47c7`

**❓ Prompt của user:**

> Fix bug: click "Write a Review" cho product "Urban Hoodie" nhưng lại redirect sang review page của product khác ("Slim Fit Jeans"). Console báo "Error fetching order: {}".

**✅ Câu trả lời / Việc đã làm:**

- Xác định nguyên nhân: `productId` không được truyền đúng khi có nhiều sản phẩm trong cùng 1 đơn
- Fix để đảm bảo đúng `productId` được gắn vào link "Write Review" của từng sản phẩm cụ thể

**📁 Files bị ảnh hưởng:**

- `src/Frontend/` (trang my-orders, write-review)

---

## 📅 Session 4 — 2026-03-03 (Khoảng 10:33 – 10:39 GMT+7)

### 🔧 Dashboard Data Discrepancy

**Conversation ID:** `21f22731-2f41-418c-bf35-b9f97730abd2`

**❓ Prompt của user:**

> Tại sao tổng đơn hàng hiển thị trên dashboard (26) khác với số đơn trong biểu đồ sales chart? Cần đồng bộ dữ liệu.

**✅ Câu trả lời / Việc đã làm:**

- Tìm ra nguyên nhân chênh lệch: dashboard count tất cả status, còn chart chỉ lọc một số trạng thái
- Thống nhất logic để cả 2 nơi dùng cùng điều kiện lọc

**📁 Files bị ảnh hưởng:**

- `src/Frontend/admin-dashboard.html` — Đồng bộ logic đếm đơn hàng

---

## 📅 Session 5 — 2026-03-04 (Khoảng 07:37 – 07:47 GMT+7)

### 🔧 Display Shipping Address in Order Details Modal

**Conversation ID:** `2d1731e2-42da-4d5f-83e4-b54aa266b851`

**❓ Prompt của user:**

> Hiển thị địa chỉ giao hàng trong modal Order Details. Cần fetch và hiển thị tên người nhận, số điện thoại, địa chỉ.

**✅ Câu trả lời / Việc đã làm:**

- Thêm fetch địa chỉ giao hàng vào logic load order detail
- Hiển thị thông tin: tên người nhận, SĐT, địa chỉ đầy đủ trong modal
- Fix lỗi modal bị trống vì thiếu trường `shippingAddress`

**📁 Files bị ảnh hưởng:**

- `src/Frontend/admin-dashboard.html` — Thêm section shipping address trong Order Detail modal

---

## 📅 Session 6 — 2026-03-04 (Khoảng 07:52 – 08:41 GMT+7)

### 🔧 Add Order Cancellation Email

**Conversation ID:** `cd2c2585-25f1-41a2-a114-33f42ec9c4ff`

**❓ Prompt của user:**

> Triển khai hệ thống gửi email thông báo khi đơn hàng bị hủy. Bao gồm template email và tích hợp vào flow xử lý đơn hàng.

**✅ Câu trả lời / Việc đã làm:**

- Tạo HTML template email hủy đơn (cho cả customer lẫn admin)
- Thêm hàm `buildOrderCancelledHtml()` + `sendOrderCancelledEmail()` vào EmailService
- Tích hợp gửi email vào flow hủy đơn (cả user cancel và admin cancel)
- Thêm field `cancelledBy` để phân biệt ai hủy đơn

**📁 Files bị ảnh hưởng:**

- `src/Backend/…/common/service/EmailService.java` — Thêm 2 method email hủy đơn

---

## 📅 Session 7 — 2026-03-04 (Khoảng 09:22 – 09:30 GMT+7)

### 🔧 Out of Stock Product Handling

**Conversation ID:** `5ad9604c-00fa-4f90-8618-9c1c9a18db95`

**❓ Prompt của user:**

> Hiển thị chữ đỏ "Hết hàng" và disable nút "Mua Ngay" / "Thêm vào giỏ" khi sản phẩm hết hàng.

**✅ Câu trả lời / Việc đã làm:**

- Thêm logic kiểm tra `stock === 0` trên trang product-detail
- Hiển thị text đỏ "Sản phẩm đã hết hàng"
- Disable (vô hiệu hóa) nút "Mua Ngay" và "Thêm vào Giỏ hàng" khi hết stock
- Nút tự động enable lại khi stock > 0

**📁 Files bị ảnh hưởng:**

- `src/Frontend/product-detail.html` — Thêm out-of-stock UI state

---

## 📅 Session 8 — 2026-03-04 (Khoảng 09:49 – 11:26 GMT+7)

### 🔧 Standardize Timezone To Vietnam (GMT+7)

**Conversation ID:** `3286c515-1f68-4447-8161-2f8c98182bc3`

**❓ Prompt của user:**

> Toàn bộ hệ thống đang bị trộn lẫn 2 timezone: flash-sale dùng UTC, các module khác dùng JVM local (GMT+7). Cần chuẩn hóa về giờ Việt Nam.

**✅ Câu trả lời / Việc đã làm:**

**🆕 Files tạo mới:**

- `TimezoneConfig.java` — `@PostConstruct` force JVM timezone = `Asia/Ho_Chi_Minh`, in log xác nhận khi startup

**✏️ Files sửa đổi:**

- `application.properties` — Thêm `spring.jackson.time-zone=Asia/Ho_Chi_Minh` + date-format
- `FlashSaleServiceImpl.java` — Xóa `ZoneOffset.UTC`, đổi sang `LocalDateTime.now()`
- `FlashSaleScheduler.java` — Xóa `ZoneOffset.UTC`, đổi sang `LocalDateTime.now()`
- `FlashSaleCampaign.java` (entity) — Xóa `@JsonFormat(timezone="UTC")`
- `FlashSale.java` (entity) — Xóa `@JsonFormat(timezone="UTC")`
- `FlashSaleSlotRequest.java` (DTO) — Xóa UTC `@JsonFormat`
- `FlashSaleCampaignRequest.java` (DTO) — Xóa UTC `@JsonFormat`
- `EmailService.java` — Xóa logic convert UTC→VN (không còn cần vì đã là VN)
- `admin-dashboard.html` — Gửi local time thay vì UTC lên server

**🗑️ Logic đã xóa:**

- Tất cả `ZoneOffset.UTC` trong flash-sale module
- Tất cả `@JsonFormat(timezone="UTC")` trên entities/DTOs
- Logic convert UTC → Vietnam time trong EmailService (vì đã là VN rồi)

---

## 📅 Session 9 — 2026-03-04 (Khoảng 12:21 – 12:24 GMT+7)

### 🔧 Fix Flash Sale Visibility After Registration Deadline

**Conversation ID:** `59dde627-9395-465d-94e1-f7f1a5106997`

**❓ Prompt của user:**

> Flash sale campaign bị ẩn đi sau khi qua deadline đăng ký. Seller cần vẫn thấy campaign dù đã hết hạn đăng ký, nhưng phải hiển thị status "Registration Closed".

**✅ Câu trả lời / Việc đã làm:**

- Sửa backend: bỏ filter loại trừ campaign đã hết deadline đăng ký khỏi danh sách trả về cho seller
- Sửa frontend: thay vì ẩn campaign, hiển thị badge/status "Đã đóng đăng ký" (Registration Closed)
- Seller vẫn thấy lịch sử campaign, chỉ không thể đăng ký mới

**📁 Files bị ảnh hưởng:**

- `src/Backend/…/flashsale/service/impl/FlashSaleServiceImpl.java` — Bỏ filter deadline
- `src/Frontend/flash-sale.html` hoặc `seller-dashboard.html` — Hiển thị "Registration Closed" status

---

## 📅 Session 10 — 2026-03-04 (Khoảng 12:53 – 13:05 GMT+7)

### 🔧 Fixing Flash Sale Issues (Code Review Fixes)

**Conversation ID:** `4a7bd81b-facc-46bd-911f-d3653d92c6ca`

**❓ Prompt của user:**

> Dựa trên code review trước, fix các vấn đề: error message sai "(UTC)", double save thừa trong FlashSaleScheduler, và email bị spam khi re-open campaign từ FINISHED.

**✅ Câu trả lời / Việc đã làm:**

**✏️ Files sửa đổi:**

- `FlashSaleServiceImpl.java`:
  - Fix error message `validateCampaignTimeline` bỏ chuỗi "(UTC)" gây hiểu nhầm (giờ đã là VN time)
  - Thêm guard: `createSlot` → không gửi broadcast email nếu campaign đang re-open từ trạng thái FINISHED
- `FlashSaleScheduler.java`:
  - Fix **double save variant**: trong reset block, entity đã được save 1 lần rồi, bỏ lần save dư thứ 2

**🗑️ Logic đã xóa:**

- Chuỗi `"(UTC)"` trong thông báo lỗi validate timeline
- Lần `save()` dư thừa trong FlashSaleScheduler reset block

---

## 📊 Tổng Kết Thống Kê

| Hạng mục                    | Số lượng                  |
| --------------------------- | ------------------------- |
| Tổng số session             | 10                        |
| Files tạo mới (NEW)         | 1 (`TimezoneConfig.java`) |
| Files sửa đổi (MODIFIED)    | ~15+ files                |
| Logic/Code đã xóa (DELETED) | 5+ đoạn code              |

### 📁 Danh sách files chính đã được chỉnh sửa:

| File                            | Thay đổi                                                 |
| ------------------------------- | -------------------------------------------------------- |
| `TimezoneConfig.java`           | 🆕 Tạo mới — Force JVM = Asia/Ho_Chi_Minh                |
| `application.properties`        | ✏️ Thêm Jackson timezone config                          |
| `FlashSaleServiceImpl.java`     | ✏️ Xóa UTC, fix error msg, fix email spam                |
| `FlashSaleScheduler.java`       | ✏️ Xóa UTC, fix double save                              |
| `FlashSaleCampaign.java`        | ✏️ Xóa @JsonFormat UTC                                   |
| `FlashSale.java`                | ✏️ Xóa @JsonFormat UTC                                   |
| `FlashSaleSlotRequest.java`     | ✏️ Xóa @JsonFormat UTC                                   |
| `FlashSaleCampaignRequest.java` | ✏️ Xóa @JsonFormat UTC                                   |
| `EmailService.java`             | ✏️ Xóa logic convert UTC→VN; thêm email hủy đơn          |
| `admin-dashboard.html`          | ✏️ Fix timezone + Order Details modal + dashboard filter |
| `product-detail.html`           | ✏️ Thêm out-of-stock UI state                            |
| `seller-dashboard.html`         | ✏️ Flash sale visibility fix                             |
