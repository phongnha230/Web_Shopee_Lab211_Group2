package simulator;

/**
 * Cấu hình toàn bộ cho simulator.
 * Chỉnh sửa tại đây trước khi chạy tool.
 */
public class SimulatorConfig {

    // ─── URL server đang chạy ───────────────────────────────────────────────
    public static final String BASE_URL = "http://localhost:8080";
    public static final String ORDER_ENDPOINT = BASE_URL + "/api/flash-sales/order";

    // ─── ID của sản phẩm muốn test ──────────────────────────────────────────
    // TODO: Điền danh sách các variantId thật từ bảng flash_sale_items trong
    // MongoDB
    public static final String[] VARIANT_IDS = {
            "699d2182371bf842b00a7164",
            "699c234900573656b4ee4654",
            "699d208d6420a004013b8c3b",
            "699bd2ee9b7f4e08c7210171"
    };

    // ─── Tham số bắn request ────────────────────────────────────────────────
    /** Tổng số request sẽ gửi (mô phỏng số user vào flash sale) */
    public static final int TOTAL_REQUESTS = 1000;

    /** Số thread xử lý đồng thời (thread pool size) */
    public static final int CONCURRENT_THREADS = 100;

    /** Số lượng mua mỗi request (thường = 1) */
    public static final int QUANTITY_PER_ORDER = 1;

    /** Timeout kết nối (ms) */
    public static final int CONNECT_TIMEOUT_MS = 5000;

    /** Timeout đọc response (ms) */
    public static final int READ_TIMEOUT_MS = 10000;

    /** Delay giữa các request batch (ms) - 0 = không delay */
    public static final int REQUEST_DELAY_MS = 0;

    // ─── Hiển thị ────────────────────────────────────────────────────────────
    /** In progress mỗi N request */
    public static final int PROGRESS_INTERVAL = 100;
}
