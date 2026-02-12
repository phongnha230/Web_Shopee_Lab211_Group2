package com.shoppeclone.backend.promotion.flashsale.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FlashSaleItemResponse {
    private String id;
    private String flashSaleId;
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
}
