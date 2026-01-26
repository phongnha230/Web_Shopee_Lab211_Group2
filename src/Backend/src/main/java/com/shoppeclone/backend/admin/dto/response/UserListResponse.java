package com.shoppeclone.backend.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    private List<AdminUserResponse> users;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}
