package com.shoppeclone.backend.promotion.flashsale.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FlashSaleCampaignRequest {
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer minDiscountPercentage;
    private Integer minStockPerProduct;
    private LocalDateTime registrationDeadline;
    private LocalDateTime approvalDeadline;
}
