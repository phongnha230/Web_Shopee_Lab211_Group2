package com.shoppeclone.backend.auth.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "roles")
@Data
public class Role {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String name; // ROLE_USER, ROLE_ADMIN, ROLE_SELLER
    
    private String description;
}