# PHƯƠNG PHÁP QUẢN LÝ FLASH SALE (TOÀN SÀN)

Đây là quy trình vận hành Flash Sale chuyên nghiệp dành cho Admin và Shop, tích hợp các cơ chế kiểm soát giá và hệ thống thông báo đa kênh.

---

### 1. QUY TRÌNH VẬN HÀNH (5 BƯỚC)

#### ⚡ Giai đoạn 1: Thiết lập Sự kiện (Admin)
- Admin tạo chiến dịch chủ chốt.
- Chia các **Khung giờ (Slots)**.
- **Cấu hình Luật chơi Linh hoạt & Timeline:** Admin thiết lập riêng cho từng chiến dịch:
    - **Mức giảm giá tối thiểu:** (Ví dụ: 10%, 20%, 50%... tùy đợt sale).
    - **Số lượng tồn kho tối thiểu:** Đảm bảo Shop có đủ hàng để bán.
    - **Hạn chót Đăng ký & Xét duyệt:** Được hệ thống tự động kiểm soát.
- **Tự động mời gọi (Auto-Invitation Broadcast):** Ngay sau khi nhấn "Create Campaign", hệ thống tự động quét danh sách toàn bộ Shop và gửi Lời mời kèm Link đăng ký qua Email & In-app.

#### ⚡ Giai đoạn 2: Shop Đăng ký & Hệ thống Kiểm tra (Shop + System)
- **Cơ chế Inventory Locking (Real-time):** Ngay khi Shop nhấn "Đăng ký", hệ thống lập tức trừ số lượng hàng đăng ký vào tồn kho gốc của `ProductVariant` để "giữ chỗ". Điều này ngăn chặn việc Shop đăng ký ảo hoặc bán quá số lượng thực tế hiện có.
- **Đồng bộ Tồn kho Cha (Product Sync):** Sau khi trừ kho ở phân loại (variant), hệ thống tự động tính toán lại và cập nhật `totalStock` của sản phẩm chính để dữ liệu trên toàn sàn luôn nhất quán.
- **Tối ưu hóa UX (Frontend Resilience):**
    - **Dynamic Avail Display:** Trong Modal đăng ký, nhãn "Avail" (Tồn kho khả dụng) sẽ tự động giảm trừ theo số lượng Shop nhập vào thời gian thực, giúp Shop dễ dàng cân đối hàng hóa.
    - **Auto-Refresh Dropdown:** Sau mỗi lần đăng ký thành công (Submit & Continue), danh sách sản phẩm sẽ được tải lại để cập nhật số lượng tồn kho mới nhất trong menu thả xuống.
    - **Parallel Fetching:** Hệ thống sử dụng `Promise.all` để tải đồng thời danh sách khung giờ (Slots) và sản phẩm, giúp Modal mở nhanh hơn.
- **Cơ chế Price Guard (Động):** Hệ thống tự động kiểm tra giá dựa trên luật của từng chiến dịch (vừa thiết lập ở Bước 1) thay vì fix cứng 10%.

#### ⚡ Giai đoạn 3: Xét duyệt & Xử lý (Admin + System)
- Admin duyệt danh sách dài các shop đăng ký bằng bộ lọc (ngành hàng, độ giảm giá).
- **Trạng thái Duyệt:** Nếu Admin từ chối (Reject), hệ thống sẽ tự động hoàn trả (revert) số lượng hàng đã khấu trừ về lại kho gốc của Shop.

#### ⚡ Giai đoạn 4: Hệ thống Thông báo Đa kênh (Multi-channel Notification)
Thông báo được gửi qua 2 kênh chính: **Email** và **In-app (Dấu chuông trên Web)**.
- **Lời mời tự động (Broadcast):** Hệ thống tự động gửi ngay khi Admin tạo chiến dịch mới. Thông báo chứa link trực tiếp đến Form đăng ký để tối ưu tỷ lệ tham gia của Shop.
- **Kết quả duyệt:** 
    - *Được duyệt:* "Chúc mừng sản phẩm [A] đã lên sóng lúc 12h".
    - *Bị từ chối:* "Sản phẩm [B] không đạt yêu cầu giá, vui lòng sửa lại".
- **Nhắc nhở:** Gửi 15 phút trước khi phiên Flash Sale bắt đầu.

- **Tự động hóa (FlashSaleScheduler):** Hệ thống sử dụng Cron Job/Fixed Rate Task để tự động kiểm tra và chuyển trạng thái:
    - **Đồng bộ thời gian UTC:** Toàn bộ hệ thống backend chạy theo múi giờ `ZoneOffset.UTC` để đảm bảo tính đồng nhất tuyệt đối với chuỗi ISO từ Frontend, bất kể server đặt ở đâu.
    - `REGISTRATION_OPEN` -> `ONGOING` (Khi đến ngày bắt đầu chiến dịch).
    - `ONGOING` -> `FINISHED` (Khi hết ngày kết thúc chiến dịch).
    - **Vòng đời của Slot:** `ACTIVE` (Chờ đến giờ) -> `ONGOING` (Đang diễn ra) -> `FINISHED` (Kết thúc).
    - Kích hoạt/Hết hạn các **Slots** (Tự động cập nhật giá sản phẩm và khóa kho).
- **Trưng bày Trang chủ (Home Display):**
    - Chỉ những Slot có trạng thái **ACTIVE** (chưa đến giờ) hoặc **ONGOING** (đang diễn ra) mới được hiển thị.
    - Sản phẩm chỉ xuất hiện khi khung giờ đạt trạng thái **ONGOING**.
- **Giao diện Real-time & Tâm lý học (FOMO):**
    - **Countdown Timer:** Đếm ngược từng giây đến khi phiên sale kết thúc.
    - **Progress Bar "Cháy hàng":** Hiển thị "Đã bán X" kèm hiệu ứng **"Blowing Fire" 🔥** và thông báo "SẮP CHÁY HÀNG" khi tồn kho còn dưới 20% (hoặc đã bán trên 80%).
- **Dừng khẩn cấp (Emergency Stop Control):** Admin có quyền dừng ngay lập tức:
    - **Dừng cả Chiến dịch:** Kết thúc tất cả các khung giờ thuộc chiến dịch đó.
    - **Dừng khung giờ (Slot):** Gỡ sản phẩm, **tự động hoàn trả giá gốc** và **trả lại tồn kho** về kho chính của Shop.
    - **Thông báo:** Hệ thống tự động gửi thông báo "Emergency Stop ⚠️" cho tất cả các Shop bị ảnh hưởng.

---

### 2. CÁC ĐIỂM THEN CHỐT CẦN LƯU Ý

1. **Minh bạch (Transparency):** Shop nhận được lý do từ chối hoặc lý do dừng khẩn cấp qua Mail/In-app.
2. **Chống gian lận (Anti-fraud):** Price Guard động đảm bảo mức giảm giá luôn thực chất theo đúng mục tiêu của từng đợt sale.
3. **Ảo hóa tồn kho:** Đảm bảo không bao giờ xảy ra tình trạng khách mua được nhưng shop báo hết hàng.
4. **Tốc độ truyền tin:** Cơ chế Broadcast tự động giúp thông tin chiến dịch đến tay hàng ngàn Shop chỉ trong tích tắc sau khi Admin tạo Campaign.
5. **Vị trí hiển thị:** Admin ưu tiên duyệt các shop có uy tín cao (Mall, Shop Đặc biệt) lên đầu trang Flash Sale.

---

### 3. CẤU TRÚC DỮ LIỆU NÂNG CẤP (DATABASE)
- `FlashSaleCampaign`: ID, Name, Desc, StartDate, EndDate, Status, **MinDiscountPercentage** (Luật giảm giá), **MinStockPerProduct** (Luật số lượng).
- `FlashSaleSlot`: ID, CampaignID, StartTime, EndTime, Status.
### 4. LỊCH TRÌNH THÔNG BÁO & ĐĂNG KÝ CHUYÊN NGHIỆP

Để vận hành sàn thương mại điện tử chuyên nghiệp và thực tế (như Shopee/Lazada), Admin cần tuân thủ lộ trình thời gian sau:

#### 🔥 1. Flash Sale nhỏ / Hàng ngày (Sale thường)
*Mục tiêu: Xả kho nhanh, tạo thói quen truy cập hàng ngày.*
- **Thời gian thông báo:** 3 – 5 ngày trước khi bắt đầu.
- **Lý do:** Shop sử dụng hàng có sẵn, không cần chuẩn bị quá nhiều về nhân sự hay banner.
- **Quy trình:**
    - Thông báo & Mở cổng: Ngày T-5.
    - Deadline đăng ký: Ngày T-2.
    - Duyệt xong: Ngày T-1.

#### 🎉 2. Mega Campaign (11.11, 12.12, Black Friday...)
*Mục tiêu: Bùng nổ doanh số, thu hút người dùng mới.*
- **Thời gian thông báo:** 3 – 4 tuần trước khi bắt đầu.
- **Lý do:** Shop cần thời gian nhập hàng số lượng lớn, thiết kế banner riêng, tính toán voucher cộng dồn.
- **Quy trình chuẩn đồ án:**
    - **Thông báo & Mở cổng:** Ngày T-21.
    - **Deadline đăng ký:** Ngày T-10 (để Shop có thời gian chốt số lượng hàng).
    - **Duyệt hoàn tất:** Ngày T-5 (để Shop chuẩn bị đóng gói và vận chuyển).

---

### [KẾT LUẬN]
Hệ thống Flash Sale này được thiết kế không chỉ để chạy code, mà còn để vận hành một mô hình kinh doanh thực thụ. Việc tuân thủ các quy tắc về **Giá (Price Guard)**, **Tồn kho (Inventory Locking)** và **Thời gian (Scheduling)** sẽ đảm bảo sự uy tín cho sàn và sự công bằng cho tất cả các Shop.
