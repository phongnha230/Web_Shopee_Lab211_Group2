# ⚡ Flash Sale Simulator Tool

Tool bắn hàng nghìn request cùng lúc vào server để test Flash Sale.

## Cấu trúc

```
FlashSaleSimulator/
├── pom.xml
└── src/main/java/simulator/
    ├── FlashSaleSimulator.java   # Main — entry point
    ├── SimulatorConfig.java      # ⚙️ SỬA FILE NÀY TRƯỚC KHI CHẠY
    ├── SimulatorStats.java       # Thread-safe counters
    ├── OrderWorker.java          # Runnable — 1 thread = 1 user
    └── ResultsPrinter.java       # In bảng ASCII kết quả
```

## Trước khi chạy

1. Chạy Spring Boot backend (port 8080)
2. Mở `SimulatorConfig.java`, thay `VARIANT_ID`:
   - Vào MongoDB → collection `product_variants`
   - Tìm 1 variant đang trong Flash Sale ONGOING
   - Copy `_id` dán vào `VARIANT_ID`
3. Chỉnh số request / thread nếu muốn

## Chạy tool

```bash
# Trong thư mục tools/FlashSaleSimulator/
mvn compile exec:java
```

Hoặc build fat jar:

```bash
mvn package
java -jar target/flash-sale-simulator-1.0.0-jar-with-dependencies.jar
```

## Kết quả mong đợi

```
╔══════════════════════════════════════════════╗
║        ⚡ FLASH SALE SIMULATOR — KẾT QUẢ ⚡  ║
╠══════════════════════════════════════════════╣
║ Tổng request gửi: 1000                       ║
║ Threads đồng thời: 100                       ║
╠══════════════════════════════════════════════╣
║ ✅ Đặt hàng thành công  : 50                 ║
║ ❌ Hết hàng (từ chối)   : 950                ║
║ 💥 Lỗi server/network   : 0                  ║
╠══════════════════════════════════════════════╣
║ STOCK INTEGRITY CHECK: ✅ PASSED             ║
╚══════════════════════════════════════════════╝
```

**Success (50) = stock ban đầu → không âm kho ✅**

## Nếu thấy lỗi "No active flash sale"

→ Variant đó chưa có trong Flash Sale ONGOING.
→ Admin cần tạo Flash Sale slot và approve product trước.

## API Endpoint backend (không cần JWT)

```
POST http://localhost:8080/api/flash-sales/order
Body: { "variantId": "xxx", "quantity": 1 }
```
