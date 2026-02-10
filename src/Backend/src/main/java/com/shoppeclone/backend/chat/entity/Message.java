package com.shoppeclone.backend.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "messages")
@Data
public class Message {
    @Id
    private String id;

    @Indexed
    private String conversationId;

    private String senderId; // User ID or Shop ID (or User ID of shop owner)
    // To distinguish, we can check if senderId matches conversation.userId or
    // conversation.shopId (if shopId is actually an ID)
    // Or we add a type

    private boolean isShopMessage; // true if sent by shop, false if sent by user

    private String content;

    @CreatedDate
    private LocalDateTime createdAt;
}
