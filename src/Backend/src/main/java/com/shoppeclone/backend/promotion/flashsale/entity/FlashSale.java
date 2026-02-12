package com.shoppeclone.backend.promotion.flashsale.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "flash_sales")
@Data
public class FlashSale {
    @Id
    private String id;

    private String campaignId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // ACTIVE, INACTIVE
}
