package com.shoppeclone.backend.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "conversations")
@Data
@CompoundIndex(name = "user_shop_idx", def = "{'userId': 1, 'shopId': 1}", unique = true)
public class Conversation {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String shopId;

    private String lastMessageContent;
    private LocalDateTime lastMessageTime;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
