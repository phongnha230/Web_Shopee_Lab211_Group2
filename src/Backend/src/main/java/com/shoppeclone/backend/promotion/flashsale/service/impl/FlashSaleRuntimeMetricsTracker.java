package com.shoppeclone.backend.promotion.flashsale.service.impl;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class FlashSaleRuntimeMetricsTracker {

    private static final long LIVE_WINDOW_MS = 5000;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final ConcurrentMap<String, SlotMetrics> metricsByFlashSaleId = new ConcurrentHashMap<>();

    public void record(String flashSaleId, String status, long responseMs) {
        if (flashSaleId == null || flashSaleId.isBlank()) {
            return;
        }
        metricsByFlashSaleId.computeIfAbsent(flashSaleId, ignored -> new SlotMetrics())
                .record(status, responseMs);
    }

    public Snapshot snapshot(String flashSaleId) {
        if (flashSaleId == null || flashSaleId.isBlank()) {
            return Snapshot.empty();
        }
        SlotMetrics metrics = metricsByFlashSaleId.get(flashSaleId);
        return metrics == null ? Snapshot.empty() : metrics.snapshot();
    }

    public static final class Snapshot {
        private final long totalRequests;
        private final long successCount;
        private final long outOfStockCount;
        private final long errorCount;
        private final long avgResponseMs;
        private final long minResponseMs;
        private final long maxResponseMs;
        private final String monitorStatus;
        private final String monitorStartedAt;
        private final String lastRequestAt;

        private Snapshot(long totalRequests, long successCount, long outOfStockCount, long errorCount,
                long avgResponseMs, long minResponseMs, long maxResponseMs,
                String monitorStatus, String monitorStartedAt, String lastRequestAt) {
            this.totalRequests = totalRequests;
            this.successCount = successCount;
            this.outOfStockCount = outOfStockCount;
            this.errorCount = errorCount;
            this.avgResponseMs = avgResponseMs;
            this.minResponseMs = minResponseMs;
            this.maxResponseMs = maxResponseMs;
            this.monitorStatus = monitorStatus;
            this.monitorStartedAt = monitorStartedAt;
            this.lastRequestAt = lastRequestAt;
        }

        public static Snapshot empty() {
            return new Snapshot(0, 0, 0, 0, 0, 0, 0, "IDLE", null, null);
        }

        public long getTotalRequests() {
            return totalRequests;
        }

        public long getSuccessCount() {
            return successCount;
        }

        public long getOutOfStockCount() {
            return outOfStockCount;
        }

        public long getErrorCount() {
            return errorCount;
        }

        public long getAvgResponseMs() {
            return avgResponseMs;
        }

        public long getMinResponseMs() {
            return minResponseMs;
        }

        public long getMaxResponseMs() {
            return maxResponseMs;
        }

        public String getMonitorStatus() {
            return monitorStatus;
        }

        public String getMonitorStartedAt() {
            return monitorStartedAt;
        }

        public String getLastRequestAt() {
            return lastRequestAt;
        }
    }

    private static final class SlotMetrics {
        private final AtomicLong totalRequests = new AtomicLong();
        private final AtomicLong successCount = new AtomicLong();
        private final AtomicLong outOfStockCount = new AtomicLong();
        private final AtomicLong errorCount = new AtomicLong();
        private final AtomicLong totalResponseMs = new AtomicLong();
        private final AtomicLong minResponseMs = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxResponseMs = new AtomicLong();
        private volatile long firstRequestAt;
        private volatile long lastRequestAt;

        private void record(String status, long responseMs) {
            long now = System.currentTimeMillis();
            if (firstRequestAt == 0L) {
                firstRequestAt = now;
            }
            lastRequestAt = now;
            totalRequests.incrementAndGet();
            totalResponseMs.addAndGet(responseMs);
            minResponseMs.accumulateAndGet(responseMs, Math::min);
            maxResponseMs.accumulateAndGet(responseMs, Math::max);

            if ("SUCCESS".equalsIgnoreCase(status)) {
                successCount.incrementAndGet();
            } else if ("OUT_OF_STOCK".equalsIgnoreCase(status)) {
                outOfStockCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
            }
        }

        private Snapshot snapshot() {
            long total = totalRequests.get();
            long min = minResponseMs.get();
            long max = maxResponseMs.get();
            long last = lastRequestAt;
            String monitorStatus = last > 0 && (System.currentTimeMillis() - last) <= LIVE_WINDOW_MS ? "LIVE" : "IDLE";
            return new Snapshot(
                    total,
                    successCount.get(),
                    outOfStockCount.get(),
                    errorCount.get(),
                    total > 0 ? totalResponseMs.get() / total : 0,
                    min == Long.MAX_VALUE ? 0 : min,
                    max,
                    monitorStatus,
                    firstRequestAt > 0 ? FORMATTER.format(Instant.ofEpochMilli(firstRequestAt)) : null,
                    last > 0 ? FORMATTER.format(Instant.ofEpochMilli(last)) : null);
        }
    }
}
