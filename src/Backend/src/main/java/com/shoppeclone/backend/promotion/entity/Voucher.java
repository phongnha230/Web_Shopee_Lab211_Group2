package com.shoppeclone.backend.promotion.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "vouchers")
@Data
public class Voucher {
    @Id
    private String id;

    private String code;
    private BigDecimal discount; // Amount or Percentage? Assuming Amount based on simple context, but could be
                                 // either. Schema just says discount.
    private Integer quantity;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
