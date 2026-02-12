package com.shoppeclone.backend.promotion.flashsale.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FlashSaleRegistrationRequest {
    private String slotId;
    private String productId;
    private String variantId;
    private BigDecimal salePrice;
    private Integer saleStock;
}
