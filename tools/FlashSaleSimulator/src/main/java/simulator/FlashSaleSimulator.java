package simulator;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ╔══════════════════════════════════════════╗
 * ║ FLASH SALE SIMULATOR — MAIN ║
 * ╚══════════════════════════════════════════╝
 *
 * Mô phỏng hàng nghìn user cùng lúc bấm mua Flash Sale.
 *
 * CÁCH CHẠY:
 * 1. Sửa SimulatorConfig.java → điền VARIANT_ID thật
 * 2. Chạy backend Spring Boot
 * 3. Trong thư mục tools/FlashSaleSimulator/:
 * mvn compile exec:java
 * Hoặc build fat jar:
 * mvn package
 * java -jar target/flash-sale-simulator-1.0.0-jar-with-dependencies.jar
 */
public class FlashSaleSimulator {

    public static void main(String[] args) throws InterruptedException {
        printBanner();

        // ── Validate config ────────────────────────────────────────────────
        if (SimulatorConfig.VARIANT_IDS == null || SimulatorConfig.VARIANT_IDS.length == 0
                || "REPLACE_WITH_REAL_VARIANT_ID_1".equals(SimulatorConfig.VARIANT_IDS[0])) {
            System.err.println("❌ LỖI: Chưa điền danh sách variantId!");
            System.err.println("   Mở SimulatorConfig.java và thay VARIANT_IDS bằng các ID thật từ MongoDB.");
            System.exit(1);
        }

        // ── Interactive setup (optional) ───────────────────────────────────
        int totalRequests = SimulatorConfig.TOTAL_REQUESTS;
        int threads = SimulatorConfig.CONCURRENT_THREADS;
        int initialStock = 0;

        System.out.println("📌 Cấu hình hiện tại:");
        System.out.println("   URL      : " + SimulatorConfig.ORDER_ENDPOINT);
        System.out.println("   Sản phẩm : test ngẫu nhiên " + SimulatorConfig.VARIANT_IDS.length + " IDs");
        System.out.println("   Requests : " + totalRequests);
        System.out.println("   Threads  : " + threads);
        System.out.println();

        System.out.print("Nhập stock ban đầu (để check integrity, nhấn Enter bỏ qua): ");
        try (Scanner scanner = new Scanner(System.in)) {
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                initialStock = Integer.parseInt(input);
            }
        } catch (Exception ignored) {
        }

        System.out.println();
        System.out.println("🚀 Bắt đầu bắn " + totalRequests + " request với " + threads + " thread đồng thời...");
        System.out.println("─".repeat(60));

        // ── Run simulation ─────────────────────────────────────────────────
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

        // ── Print results ──────────────────────────────────────────────────
        System.out.println("─".repeat(60));
        System.out.println("✅ Tất cả request đã xong!");
        ResultsPrinter.printResults(stats, wallEnd - wallStart, initialStock);
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.println("  ║     ⚡ SHOPEE FLASH SALE SIMULATOR v1.0 ⚡       ║");
        System.out.println("  ║        Bắn hàng nghìn request cùng lúc          ║");
        System.out.println("  ║        Test atomic stock — không âm kho          ║");
        System.out.println("  ╚══════════════════════════════════════════════════╝");
        System.out.println();
    }
}
