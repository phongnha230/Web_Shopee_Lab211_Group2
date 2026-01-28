package com.shoppeclone.backend.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private String id;
    private String email;
    private String fullName;
    private String phone;
    private Set<String> roles;
    private boolean emailVerified;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
