package simulator;

/**
 * In kết quả dạng bảng ASCII đẹp ra console.
 */
public class ResultsPrinter {

    private static final String BORDER = "╠══════════════════════════════════════════════╣";
    private static final String TOP = "╔══════════════════════════════════════════════╗";
    private static final String BOT = "╚══════════════════════════════════════════════╝";
    private static final int WIDTH = 46; // nội dung giữa 2 dấu ║

    public static void printResults(SimulatorStats stats, long totalMs, int initialStock) {
        System.out.println();
        System.out.println(TOP);
        printCenter("⚡ FLASH SALE SIMULATOR — KẾT QUẢ ⚡");
        System.out.println(BORDER);

        printRow("Tổng request gửi", String.valueOf(stats.getTotalSent()));
        printRow("Threads đồng thời", String.valueOf(SimulatorConfig.CONCURRENT_THREADS));
        System.out.println(BORDER);

        printRow("✅ Đặt hàng thành công", String.valueOf(stats.getSuccessCount()));
        printRow("❌ Hết hàng (từ chối)", String.valueOf(stats.getOutOfStockCount()));
        printRow("💥 Lỗi server/network", String.valueOf(stats.getErrorCount()));
        System.out.println(BORDER);

        printRow("⏱  Avg response time", stats.getAvgResponseMs() + " ms");
        printRow("🐢 Min response time", stats.getMinResponseMs() + " ms");
        printRow("🔥 Max response time", stats.getMaxResponseMs() + " ms");
        printRow("🕐 Tổng thời gian", formatMs(totalMs));
        System.out.println(BORDER);

        // Stock integrity check
        int lastStock = stats.getLastRemainingStock();
        if (lastStock >= 0) {
            printRow("📦 Stock còn lại (DB)", String.valueOf(lastStock));
        }

        // Verification
        System.out.println(BORDER);
        boolean stockOk = stats.getSuccessCount() <= initialStock
                && stats.getSuccessCount() + stats.getOutOfStockCount() + stats.getErrorCount() == stats.getTotalSent();

        if (initialStock > 0) {
            boolean exactOk = stats.getSuccessCount() <= initialStock;
            String verdict = exactOk ? "✅ PASSED" : "❌ FAILED — CÓ ÂM KHO!";
            printRow("STOCK INTEGRITY CHECK", verdict);
            if (exactOk) {
                printRow("success (" + stats.getSuccessCount() + ") ≤ stock (" + initialStock + ")", "✅ OK");
            }
        }

        System.out.println(BOT);
        System.out.println();

        // Bonus bar chart
        if (stats.getTotalSent() > 0) {
            printBarChart(stats);
        }
    }

    private static void printCenter(String text) {
        int padding = (WIDTH - text.length()) / 2;
        String line = " ".repeat(Math.max(0, padding)) + text;
        // Pad to full width
        while (line.length() < WIDTH)
            line += " ";
        System.out.println("║" + line + "║");
    }

    private static void printRow(String label, String value) {
        String content = " " + label + ": " + value;
        while (content.length() < WIDTH)
            content += " ";
        if (content.length() > WIDTH)
            content = content.substring(0, WIDTH);
        System.out.println("║" + content + "║");
    }

    private static String formatMs(long ms) {
        if (ms < 1000)
            return ms + " ms";
        return String.format("%.2f s", ms / 1000.0);
    }

    private static void printBarChart(SimulatorStats stats) {
        int total = stats.getTotalSent();
        int barMaxWidth = 30;

        System.out.println("  Phân bố kết quả:");

        int ok = stats.getSuccessCount();
        int oos = stats.getOutOfStockCount();
        int err = stats.getErrorCount();

        printBar("✅ Thành công  ", ok, total, barMaxWidth);
        printBar("❌ Hết hàng    ", oos, total, barMaxWidth);
        printBar("💥 Lỗi         ", err, total, barMaxWidth);
        System.out.println();
    }

    private static void printBar(String label, int count, int total, int maxWidth) {
        int filled = total > 0 ? (int) ((double) count / total * maxWidth) : 0;
        String bar = "█".repeat(filled) + "░".repeat(maxWidth - filled);
        double pct = total > 0 ? (double) count / total * 100 : 0;
        System.out.printf("  %s [%s] %d (%.1f%%)%n", label, bar, count, pct);
    }
}
