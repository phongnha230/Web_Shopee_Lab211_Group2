package com.shoppeclone.backend.promotion.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "flash_sales")
@Data
public class FlashSale {
    @Id
    private String id;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Status can be derived from time, but could add explicit status if needed.
}
