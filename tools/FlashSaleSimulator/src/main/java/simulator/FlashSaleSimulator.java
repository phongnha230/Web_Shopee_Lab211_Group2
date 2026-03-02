package simulator;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ╔══════════════════════════════════════════╗
 * ║ FLASH SALE SIMULATOR — MAIN v2.0 ║
 * ╚══════════════════════════════════════════╝
 *
 * Mô phỏng hàng nghìn user cùng lúc bấm mua Flash Sale.
 * ⚡ Tự động lấy variant từ API — không cần sửa code khi đổi chiến dịch!
 *
 * CÁCH CHẠY:
 * 1. Chạy backend Spring Boot
 * 2. Admin tạo & kích hoạt chiến dịch Flash Sale
 * 3. Trong thư mục tools/FlashSaleSimulator/:
 * mvn compile exec:java
 */
public class FlashSaleSimulator {

    /** Variant IDs được fetch từ API — toàn cục để OrderWorker truy cập */
    static String[] activeVariantIds;

    public static void main(String[] args) throws InterruptedException {
        printBanner();

        // ── Step 1: Auto-fetch Flash Sale đang active ─────────────────────
        System.out.println("🔍 Đang lấy Flash Sale đang active từ server...");
        System.out.println("   URL: " + SimulatorConfig.BASE_URL);
        System.out.println();

        FlashSaleApiFetcher.FlashSaleInfo flashSaleInfo = FlashSaleApiFetcher.fetchActiveFlashSale();

        if (flashSaleInfo == null || flashSaleInfo.items.isEmpty()) {
            System.err.println("❌ Không tìm thấy Flash Sale nào đang active!");
            System.err.println("   → Admin cần tạo và kích hoạt chiến dịch Flash Sale trước.");
            System.err.println("   → Kiểm tra backend đang chạy tại " + SimulatorConfig.BASE_URL);
            System.exit(1);
        }

        // ── Step 2: Hiển thị thông tin chiến dịch ─────────────────────────
        activeVariantIds = flashSaleInfo.getVariantIds();
        int totalStock = flashSaleInfo.getTotalStock();

        System.out.println("✅ Tìm thấy Flash Sale đang chạy!");
        System.out.println("   📦 Flash Sale ID : " + flashSaleInfo.flashSaleId);
        System.out.println("   ⏰ Kết thúc lúc  : " + flashSaleInfo.endTime);
        System.out.println("   🛒 Số items      : " + flashSaleInfo.items.size());
        System.out.println();

        // Bảng sản phẩm
        System.out.println("   ┌─────┬────────────────────────────────────┬────────────────────────────┬───────┐");
        System.out.println("   │  #  │ Tên sản phẩm                       │ Variant ID                 │ Stock │");
        System.out.println("   ├─────┼────────────────────────────────────┼────────────────────────────┼───────┤");

        int idx = 1;
        for (FlashSaleApiFetcher.ItemInfo item : flashSaleInfo.items) {
            String name = item.productName != null ? item.productName : "N/A";
            if (name.length() > 34)
                name = name.substring(0, 31) + "...";
            String vid = item.variantId;
            if (vid.length() > 26)
                vid = vid.substring(0, 10) + "..." + vid.substring(vid.length() - 10);
            System.out.printf("   │ %3d │ %-34s │ %-26s │ %5d │%n",
                    idx++, name, vid, item.saleStock);
        }

        System.out.println("   └─────┴────────────────────────────────────┴────────────────────────────┴───────┘");
        System.out.println();

        // ── Step 3: Cấu hình chạy ────────────────────────────────────────
        int totalRequests = SimulatorConfig.TOTAL_REQUESTS;
        int threads = SimulatorConfig.CONCURRENT_THREADS;

        System.out.println("📌 Cấu hình:");
        System.out.println("   URL      : " + SimulatorConfig.ORDER_ENDPOINT);
        System.out.println("   Sản phẩm : " + activeVariantIds.length + " variants (auto-fetched ✅)");
        System.out.println("   Requests : " + totalRequests);
        System.out.println("   Threads  : " + threads);
        System.out.println("   Tổng stock: " + totalStock);
        System.out.println();

        System.out.print("Nhấn Enter để bắt đầu (hoặc Ctrl+C để hủy): ");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        } catch (Exception ignored) {
        }

        System.out.println();
        System.out.println("🚀 Bắt đầu bắn " + totalRequests + " request với " + threads + " thread đồng thời...");
        System.out.println("─".repeat(60));

        // ── Step 4: Run simulation ──────────────────────────────────────
        SimulatorStats stats = new SimulatorStats();
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        long wallStart = System.currentTimeMillis();

        for (int i = 1; i <= totalRequests; i++) {
            executor.submit(new OrderWorker(stats, i));
        }

        // Đợi tất cả xong (tối đa 5 phút)
        executor.shutdown();
        boolean finished = executor.awaitTermination(5, TimeUnit.MINUTES);

        long wallEnd = System.currentTimeMillis();

        if (!finished) {
            System.out.println("⚠️  Một số request bị timeout sau 5 phút.");
            executor.shutdownNow();
        }

        // ── Step 5: Print results ───────────────────────────────────────
        System.out.println("─".repeat(60));
        System.out.println("✅ Tất cả request đã xong!");
        ResultsPrinter.printResults(stats, wallEnd - wallStart, totalStock);
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.println("  ║     ⚡ SHOPEE FLASH SALE SIMULATOR v2.0 ⚡       ║");
        System.out.println("  ║        Bắn hàng nghìn request cùng lúc          ║");
        System.out.println("  ║        Test atomic stock — không âm kho          ║");
        System.out.println("  ║        Auto-fetch variants từ API  🔄            ║");
        System.out.println("  ╚══════════════════════════════════════════════════╝");
        System.out.println();
    }
}
