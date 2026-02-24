package com.shoppeclone.backend.order.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItem {
    private String variantId;
    private BigDecimal price;
    private int quantity;
    private String flashSaleId; // If item was bought during flash sale

    // ── Snapshot at order time (so display works even if product is deleted) ──
    private String productName; // e.g. "Book - My Sweet Orange Tree"
    private String productImage; // primary image URL
    private String variantName; // e.g. "Default" or "Red - L"
}
