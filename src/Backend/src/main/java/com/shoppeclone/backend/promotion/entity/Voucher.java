package com.shoppeclone.backend.promotion.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "vouchers")
@Data
public class Voucher {
    @Id
    private String id;

    private String code;
    private String description; // e.g., "10% OFF Electronics"

    /** Category IDs this voucher applies to. Null/empty = applies to all products. */
    private List<String> categoryIds;
    /** Display name of restricted category (e.g. "Electronics") - for UI hint. */
    private String categoryRestriction;

    // Discount Logic
    private DiscountType discountType; // PERCENTAGE, FIXED_AMOUNT
    private BigDecimal discountValue; // 10 (for 10%) or 30 (for ₫30)
    private BigDecimal minSpend; // Minimum order value to apply
    private BigDecimal maxDiscount; // Max discount amount (for percentage)

    private Integer quantity;
    private Integer usedCount = 0; // Track usage

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private boolean isActive = true;

    private VoucherType type = VoucherType.PRODUCT; // Default to PRODUCT

    public enum VoucherType {
        PRODUCT,
        SHIPPING
    }

    public enum DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT
    }
}
