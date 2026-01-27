package com.shoppeclone.backend.admin.dto.request;

import lombok.Data;
import java.util.Set;

@Data
public class AdminUserRequest {
    private String email;
    private String fullName;
    private String phone;
    private String password; // Optional - chỉ dùng khi tạo mới hoặc reset password
    private Set<String> roles; // Ví dụ: ["ROLE_USER", "ROLE_ADMIN"]
    private Boolean active; // true = active, false = banned
}
