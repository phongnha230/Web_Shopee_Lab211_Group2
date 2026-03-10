package simulator;

/**
 * In ket qua dang bang ASCII ra console.
 */
public class ResultsPrinter {

    private static final String BORDER = "==============================================";
    private static final String TOP = "==============================================";
    private static final String BOT = "==============================================";
    private static final int WIDTH = 46;

    public static void printResults(SimulatorStats stats, long totalMs, int initialStock) {
        System.out.println();
        System.out.println(TOP);
        printCenter("FLASH SALE SIMULATOR - KET QUA");
        System.out.println(BORDER);

        printRow("Tong request gui", String.valueOf(stats.getTotalSent()));
        printRow("Threads dong thoi", String.valueOf(SimulatorConfig.CONCURRENT_THREADS));
        System.out.println(BORDER);

        printRow("Dat hang thanh cong", String.valueOf(stats.getSuccessCount()));
        printRow("Het hang (tu choi)", String.valueOf(stats.getOutOfStockCount()));
        printRow("Loi server/network", String.valueOf(stats.getErrorCount()));
        System.out.println(BORDER);

        printRow("Avg response time", stats.getAvgResponseMs() + " ms");
        printRow("Min response time", stats.getMinResponseMs() + " ms");
        printRow("Max response time", stats.getMaxResponseMs() + " ms");
        printRow("Tong thoi gian", formatMs(totalMs));
        System.out.println(BORDER);

        int lastStock = stats.getLastRemainingStock();
        if (lastStock >= 0) {
            printRow("Last variant remaining", String.valueOf(lastStock));
        }

        System.out.println(BORDER);
        if (initialStock > 0) {
            boolean exactOk = stats.getSuccessCount() <= initialStock;
            String verdict = exactOk ? "PASSED" : "FAILED - CO AM KHO";
            printRow("STOCK INTEGRITY CHECK", verdict);
            if (exactOk) {
                printRow("success (" + stats.getSuccessCount() + ") <= stock (" + initialStock + ")", "OK");
            }
        }

        System.out.println(BOT);
        System.out.println("Note: 'Last variant remaining' la stock con lai cua variant o response cuoi, khong phai tong stock toan campaign.");
        System.out.println();

        if (stats.getTotalSent() > 0) {
            printBarChart(stats);
        }
    }

    private static void printCenter(String text) {
        int padding = (WIDTH - text.length()) / 2;
        String line = " ".repeat(Math.max(0, padding)) + text;
        while (line.length() < WIDTH) {
            line += " ";
        }
        System.out.println("|" + line + "|");
    }

    private static void printRow(String label, String value) {
        String content = " " + label + ": " + value;
        while (content.length() < WIDTH) {
            content += " ";
        }
        if (content.length() > WIDTH) {
            content = content.substring(0, WIDTH);
        }
        System.out.println("|" + content + "|");
    }

    private static String formatMs(long ms) {
        if (ms < 1000) {
            return ms + " ms";
        }
        return String.format("%.2f s", ms / 1000.0);
    }

    private static void printBarChart(SimulatorStats stats) {
        int total = stats.getTotalSent();
        int barMaxWidth = 30;

        System.out.println("  Phan bo ket qua:");

        int ok = stats.getSuccessCount();
        int oos = stats.getOutOfStockCount();
        int err = stats.getErrorCount();

        printBar("Thanh cong   ", ok, total, barMaxWidth);
        printBar("Het hang     ", oos, total, barMaxWidth);
        printBar("Loi          ", err, total, barMaxWidth);
        System.out.println();
    }

    private static void printBar(String label, int count, int total, int maxWidth) {
        int filled = total > 0 ? (int) ((double) count / total * maxWidth) : 0;
        String bar = "#".repeat(filled) + "-".repeat(maxWidth - filled);
        double pct = total > 0 ? (double) count / total * 100 : 0;
        System.out.printf("  %s [%s] %d (%.1f%%)%n", label, bar, count, pct);
    }
}
