package simulator;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Gọi API backend để lấy thông tin Flash Sale đang active
 * và danh sách variant IDs tự động.
 */
public class FlashSaleApiFetcher {

    /** Kết quả chứa thông tin flash sale + danh sách items */
    public static class FlashSaleInfo {
        public String flashSaleId;
        public String endTime;
        public List<ItemInfo> items = new ArrayList<>();

        public String[] getVariantIds() {
            return items.stream().map(i -> i.variantId).toArray(String[]::new);
        }

        public int getTotalStock() {
            return items.stream().mapToInt(i -> i.saleStock).sum();
        }
    }

    public static class ItemInfo {
        public String variantId;
        public String productName;
        public int saleStock;
        public int remainingStock;
    }

    /**
     * Lấy Flash Sale đang active từ server.
     * 
     * @return FlashSaleInfo hoặc null nếu không có flash sale nào active
     */
    public static FlashSaleInfo fetchActiveFlashSale() {
        try {
            // Step 1: GET /api/flash-sales/current
            String currentJson = httpGet(SimulatorConfig.BASE_URL + "/api/flash-sales/current");
            if (currentJson == null || currentJson.isEmpty()) {
                return null;
            }

            FlashSaleInfo info = new FlashSaleInfo();
            info.flashSaleId = extractString(currentJson, "id");
            info.endTime = extractString(currentJson, "endTime");

            if (info.flashSaleId == null || info.flashSaleId.isEmpty()) {
                return null;
            }

            // Step 2: GET /api/flash-sales/{id}/items
            String itemsJson = httpGet(
                    SimulatorConfig.BASE_URL + "/api/flash-sales/" + info.flashSaleId + "/items");
            if (itemsJson != null && !itemsJson.isEmpty()) {
                info.items = parseItems(itemsJson);
            }

            return info;

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gọi API: " + e.getMessage());
            return null;
        }
    }

    // ── HTTP GET helper ────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    private static String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(SimulatorConfig.CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(SimulatorConfig.READ_TIMEOUT_MS);

        int status = conn.getResponseCode();
        if (status == 204 || status == 404) {
            conn.disconnect();
            return null;
        }
        if (status != 200) {
            conn.disconnect();
            throw new RuntimeException("HTTP " + status + " from " + urlStr);
        }

        try (InputStream is = conn.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } finally {
            conn.disconnect();
        }
    }

    // ── JSON parsing (no external library) ────────────────────────

    /** Parse JSON array of items */
    private static List<ItemInfo> parseItems(String json) {
        List<ItemInfo> items = new ArrayList<>();
        // Split by objects in the array
        json = json.trim();
        if (!json.startsWith("["))
            return items;

        // Find each {...} block
        int depth = 0;
        int objStart = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0)
                    objStart = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objStart >= 0) {
                    String objStr = json.substring(objStart, i + 1);
                    ItemInfo item = parseOneItem(objStr);
                    if (item.variantId != null && !item.variantId.isEmpty()) {
                        items.add(item);
                    }
                    objStart = -1;
                }
            }
        }
        return items;
    }

    private static ItemInfo parseOneItem(String obj) {
        ItemInfo item = new ItemInfo();
        item.variantId = extractString(obj, "variantId");
        item.productName = extractString(obj, "productName");
        item.saleStock = extractInt(obj, "saleStock", 0);
        item.remainingStock = extractInt(obj, "remainingStock", 0);
        return item;
    }

    private static String extractString(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0)
            return null;
        int colon = json.indexOf(':', idx + pattern.length());
        if (colon < 0)
            return null;
        String rest = json.substring(colon + 1).trim();
        if (!rest.startsWith("\""))
            return null;
        int end = rest.indexOf('"', 1);
        if (end < 0)
            return null;
        return rest.substring(1, end);
    }

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
