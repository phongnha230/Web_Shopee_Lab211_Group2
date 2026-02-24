package simulator;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe counters cho kết quả simulator.
 * Dùng AtomicInteger/AtomicLong → không cần synchronized.
 */
public class SimulatorStats {

    private final AtomicInteger totalSent = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger outOfStockCount = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final AtomicLong totalResponseMs = new AtomicLong(0);
    private final AtomicLong minResponseMs = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxResponseMs = new AtomicLong(0);

    private volatile int lastRemainingStock = -1;

    public void recordSuccess(long responseMs) {
        totalSent.incrementAndGet();
        successCount.incrementAndGet();
        recordTime(responseMs);
    }

    public void recordOutOfStock(long responseMs) {
        totalSent.incrementAndGet();
        outOfStockCount.incrementAndGet();
        recordTime(responseMs);
    }

    public void recordError(long responseMs) {
        totalSent.incrementAndGet();
        errorCount.incrementAndGet();
        recordTime(responseMs);
    }

    public void setLastRemainingStock(int stock) {
        this.lastRemainingStock = stock;
    }

    private void recordTime(long ms) {
        totalResponseMs.addAndGet(ms);
        // Update min
        long curMin = minResponseMs.get();
        while (ms < curMin) {
            if (minResponseMs.compareAndSet(curMin, ms))
                break;
            curMin = minResponseMs.get();
        }
        // Update max
        long curMax = maxResponseMs.get();
        while (ms > curMax) {
            if (maxResponseMs.compareAndSet(curMax, ms))
                break;
            curMax = maxResponseMs.get();
        }
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public int getTotalSent() {
        return totalSent.get();
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public int getOutOfStockCount() {
        return outOfStockCount.get();
    }

    public int getErrorCount() {
        return errorCount.get();
    }

    public int getLastRemainingStock() {
        return lastRemainingStock;
    }

    public long getAvgResponseMs() {
        int total = totalSent.get();
        return total > 0 ? totalResponseMs.get() / total : 0;
    }

    public long getMinResponseMs() {
        long v = minResponseMs.get();
        return v == Long.MAX_VALUE ? 0 : v;
    }

    public long getMaxResponseMs() {
        return maxResponseMs.get();
    }
}
