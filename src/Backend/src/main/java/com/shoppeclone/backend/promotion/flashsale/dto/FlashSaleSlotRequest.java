package com.shoppeclone.backend.promotion.flashsale.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FlashSaleSlotRequest {
    private String campaignId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
