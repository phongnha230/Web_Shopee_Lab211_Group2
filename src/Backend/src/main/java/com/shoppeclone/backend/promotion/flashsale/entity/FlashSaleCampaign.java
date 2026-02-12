package com.shoppeclone.backend.promotion.flashsale.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "flash_sale_campaigns")
@Data
public class FlashSaleCampaign {
    @Id
    private String id;

    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private String status; // UPCOMING, REGISTRATION_OPEN, ONGOING, FINISHED

    private Integer minDiscountPercentage = 10;
    private Integer minStockPerProduct = 5;

    private LocalDateTime registrationDeadline;
    private LocalDateTime approvalDeadline;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
