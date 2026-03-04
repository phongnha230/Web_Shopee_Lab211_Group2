package com.shoppeclone.backend.promotion.flashsale.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FlashSaleItemResponse {
    private String id;
    private String flashSaleId;
    private String campaignId;
    private String campaignName;
    private LocalDateTime slotStartTime;
    private LocalDateTime slotEndTime;
    private String productId;
    private String productName;
    private String productImage;
    private String variantId;
    private String variantName;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer saleStock;
    private Integer remainingStock;
    private String status;
    private String adminNote;
    private String shopId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvalDeadline; // from campaign, for frontend deadline check
}
