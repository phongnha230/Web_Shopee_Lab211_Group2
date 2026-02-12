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
- Shop điền biểu mẫu: Chọn **ID sản phẩm**, nhập **Giá Sale** và **Số lượng**.
- **Cơ chế Price Guard (Động):** Hệ thống tự động kiểm tra giá dựa trên luật của từng chiến dịch (vừa thiết lập ở Bước 1) thay vì fix cứng 10%.

#### ⚡ Giai đoạn 3: Xét duyệt & Khóa kho (Admin + System)
- Admin duyệt danh sách dài các shop đăng ký bằng bộ lọc (ngành hàng, độ giảm giá).
- **Khóa tồn kho ảo:** Ngay khi Admin bấm "Duyệt", hệ thống sẽ tách riêng số lượng hàng Flash Sale khỏi kho chung của Shop để đảm bảo luôn có hàng cho khách.

#### ⚡ Giai đoạn 4: Hệ thống Thông báo Đa kênh (Multi-channel Notification)
Thông báo được gửi qua 2 kênh chính: **Email** và **In-app (Dấu chuông trên Web)**.
- **Lời mời tự động (Broadcast):** Hệ thống tự động gửi ngay khi Admin tạo chiến dịch mới. Thông báo chứa link trực tiếp đến Form đăng ký để tối ưu tỷ lệ tham gia của Shop.
- **Kết quả duyệt:** 
    - *Được duyệt:* "Chúc mừng sản phẩm [A] đã lên sóng lúc 12h".
    - *Bị từ chối:* "Sản phẩm [B] không đạt yêu cầu giá, vui lòng sửa lại".
- **Nhắc nhở:** Gửi 15 phút trước khi phiên Flash Sale bắt đầu.

#### ⚡ Giai đoạn 5: Thực thi & UX (Automation + UX)
- **Tự động hóa (FlashSaleScheduler):** Hệ thống sử dụng Cron Job/Fixed Rate Task để tự động kiểm tra và chuyển trạng thái:
    - `REGISTRATION_OPEN` -> `ONGOING` (Khi đến ngày bắt đầu).
    - `ONGOING` -> `FINISHED` (Khi hết ngày kết thúc).
    - Kích hoạt/Hết hạn các **Slots** (Tự động cập nhật giá sản phẩm và khóa kho).
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
