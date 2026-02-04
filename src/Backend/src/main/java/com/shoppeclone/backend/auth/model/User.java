package com.shoppeclone.backend.auth.model;
// Triggering re-compilation

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "users")
@Data
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;
    private String fullName;
    private String phone;
    private String avatar;
    private boolean emailVerified = false;
    private boolean active = true;

    // @DBRef <-- Xoá dòng này để lưu Role trực tiếp vào trong User (Embed)
    private Set<Role> roles = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}