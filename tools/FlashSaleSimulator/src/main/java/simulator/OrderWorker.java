package simulator;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Một Runnable đại diện cho 1 user bấm mua Flash Sale.
 * Mỗi worker gửi 1 POST request → đọc response → ghi vào stats.
 * Không dùng external library, chỉ dùng Java standard library.
 */
public class OrderWorker implements Runnable {

    private final SimulatorStats stats;
    private final int requestNumber;

    public OrderWorker(SimulatorStats stats, int requestNumber) {
        this.stats = stats;
        this.requestNumber = requestNumber;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        HttpURLConnection conn = null;

        try {
            // Chọn ngẫu nhiên một biến thể từ danh sách auto-fetched
            String[] variantIds = FlashSaleSimulator.activeVariantIds;
            int randomIndex = (int) (Math.random() * variantIds.length);
            String selectedVariantId = variantIds[randomIndex];

            // ── Build request body ─────────────────────────────────────────
            String body = String.format(
                    "{\"variantId\":\"%s\",\"quantity\":%d,\"note\":\"simulator-request-%d\"}",
                    selectedVariantId,
                    SimulatorConfig.QUANTITY_PER_ORDER,
                    requestNumber);

            // ── Open connection ────────────────────────────────────────────
            @SuppressWarnings("deprecation")
            URL url = new URL(SimulatorConfig.ORDER_ENDPOINT);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(SimulatorConfig.CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(SimulatorConfig.READ_TIMEOUT_MS);
            conn.setDoOutput(true);

            // ── Send body ──────────────────────────────────────────────────
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            // ── Read response ──────────────────────────────────────────────
            int statusCode = conn.getResponseCode();
            long responseMs = System.currentTimeMillis() - startTime;

            java.io.InputStream is = (statusCode >= 200 && statusCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String responseBody = "";
            if (is != null) {
                responseBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            // ── Parse JSON thủ công (không cần external library) ──────────
            if (statusCode == 200 && !responseBody.isEmpty()) {
                boolean success = extractBoolean(responseBody, "success", false);
                String status = extractString(responseBody, "status", "UNKNOWN");
                int remaining = extractInt(responseBody, "remainingStock", -1);

                if (remaining >= 0) {
                    stats.setLastRemainingStock(remaining);
                }

                if (success) {
                    stats.recordSuccess(responseMs);
                } else if ("OUT_OF_STOCK".equals(status)) {
                    stats.recordOutOfStock(responseMs);
                } else {
                    stats.recordError(responseMs);
                }
            } else {
                stats.recordError(responseMs);
            }

            // ── Optional delay ─────────────────────────────────────────────
            if (SimulatorConfig.REQUEST_DELAY_MS > 0) {
                Thread.sleep(SimulatorConfig.REQUEST_DELAY_MS);
            }

        } catch (Exception e) {
            long responseMs = System.currentTimeMillis() - startTime;
            stats.recordError(responseMs);
        } finally {
            if (conn != null)
                conn.disconnect();
        }

        // ── Progress log ───────────────────────────────────────────────────
        int sent = stats.getTotalSent();
        if (sent % SimulatorConfig.PROGRESS_INTERVAL == 0) {
            System.out.printf("  [%4d sent] ✅ %d  ❌ %d  💥 %d%n",
                    sent,
                    stats.getSuccessCount(),
                    stats.getOutOfStockCount(),
                    stats.getErrorCount());
        }
    }

    // ── Lightweight JSON helpers (không cần Jackson) ──────────────────────

    /** Trích xuất giá trị boolean từ JSON string dạng {"key":true} */
    private static boolean extractBoolean(String json, String key, boolean defaultValue) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0)
            return defaultValue;
        int colon = json.indexOf(':', idx + pattern.length());
        if (colon < 0)
            return defaultValue;
        String rest = json.substring(colon + 1).trim();
        if (rest.startsWith("true"))
            return true;
        if (rest.startsWith("false"))
            return false;
        return defaultValue;
    }

    /** Trích xuất giá trị String từ JSON string dạng {"key":"value"} */
    private static String extractString(String json, String key, String defaultValue) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0)
            return defaultValue;
        int colon = json.indexOf(':', idx + pattern.length());
        if (colon < 0)
            return defaultValue;
        String rest = json.substring(colon + 1).trim();
        if (!rest.startsWith("\""))
            return defaultValue;
        int end = rest.indexOf('"', 1);
        if (end < 0)
            return defaultValue;
        return rest.substring(1, end);
    }

    /** Trích xuất giá trị int từ JSON string dạng {"key":123} */
    private static int extractInt(String json, String key, int defaultValue) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0)
            return defaultValue;
        int colon = json.indexOf(':', idx + pattern.length());
        if (colon < 0)
            return defaultValue;
        String rest = json.substring(colon + 1).trim();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rest.length(); i++) {
            char c = rest.charAt(i);
            if (Character.isDigit(c) || (i == 0 && c == '-'))
                sb.append(c);
            else if (sb.length() > 0)
                break;
        }
        if (sb.length() == 0)
            return defaultValue;
        try {
            return Integer.parseInt(sb.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
