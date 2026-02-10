package com.shoppeclone.backend.follow.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "follows")
@Data
@CompoundIndex(name = "user_shop_idx", def = "{'userId': 1, 'shopId': 1}", unique = true)
public class Follow {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String shopId;

    @CreatedDate
    private LocalDateTime createdAt;
}
