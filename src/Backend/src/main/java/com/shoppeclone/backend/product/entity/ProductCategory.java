package com.shoppeclone.backend.product.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

@Document(collection = "product_categories")
@CompoundIndex(name = "product_category_idx", def = "{'productId': 1, 'categoryId': 1}", unique = true)
@Data
public class ProductCategory {
    @Id
    private String id;
    
    private String productId; // FK to products.id
    private String categoryId; // FK to categories.id
}
