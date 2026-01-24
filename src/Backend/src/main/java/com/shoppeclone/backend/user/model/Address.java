package com.shoppeclone.backend.user.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "addresses")
@Data
public class Address {
    @Id
    private String id;
    private String userId;
    private String fullName;
    private String phone;
    private String address;
    private String city;
    private String district;
    private String ward;
    private boolean isDefault = false; // ✅ CHÍNH XÁC
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
