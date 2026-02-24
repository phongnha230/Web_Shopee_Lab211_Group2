package com.shoppeclone.backend.promotion.flashsale.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleOrderResult {
    private boolean success; // true = đặt hàng thành công
    private String status; // SUCCESS | OUT_OF_STOCK | ERROR
    private String message; // Thông báo chi tiết
    private Integer remainingStock; // Stock còn lại sau khi xử lý
    private String variantId; // Variant đã xử lý
    private long responseTimeMs; // Thời gian xử lý (ms) - dùng cho simulator
}
