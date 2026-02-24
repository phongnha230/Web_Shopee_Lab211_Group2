package com.shoppeclone.backend.promotion.flashsale.dto;

import lombok.Data;

@Data
public class FlashSaleOrderRequest {
    private String variantId; // ID của ProductVariant cần mua
    private Integer quantity; // Số lượng mua (thường = 1 khi flash sale)
    private String note; // Optional: ghi chú từ simulator
}
