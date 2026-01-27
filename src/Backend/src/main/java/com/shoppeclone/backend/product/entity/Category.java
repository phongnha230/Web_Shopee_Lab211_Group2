package com.shoppeclone.backend.product.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "categories")
@Data
public class Category {
    @Id
    private String id;
    
    private String name;
    
    // Self-referencing for hierarchical categories
    private String parentId; // FK to categories.id
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
