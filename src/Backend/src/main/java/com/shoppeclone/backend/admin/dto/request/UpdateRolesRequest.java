package com.shoppeclone.backend.admin.dto.request;

import lombok.Data;
import java.util.Set;

@Data
public class UpdateRolesRequest {
    private Set<String> roles; // Ví dụ: ["ROLE_USER", "ROLE_SELLER"]
}
