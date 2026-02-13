package com.shoppeclone.backend.promotion.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "shop_vouchers")
@Data
public class ShopVoucher {
    @Id
    private String id;

    @Indexed
    private String shopId;

    private String code;
    private BigDecimal discount;
    private BigDecimal minSpend; // Minimum order value to use this voucher
    private Integer quantity;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
