package com.shoppeclone.backend.admin.service;

import com.shoppeclone.backend.admin.dto.request.AdminUserRequest;
import com.shoppeclone.backend.admin.dto.request.UpdateRolesRequest;
import com.shoppeclone.backend.admin.dto.response.AdminUserResponse;
import com.shoppeclone.backend.admin.dto.response.UserListResponse;

public interface AdminUserService {

    /**
     * Lấy danh sách users với pagination và filter
     */
    UserListResponse getAllUsers(int page, int size, String search, String role);

    /**
     * Lấy chi tiết user theo ID
     */
    AdminUserResponse getUserById(String id);

    /**
     * Tạo user mới
     */
    AdminUserResponse createUser(AdminUserRequest request);

    /**
     * Cập nhật thông tin user
     */
    AdminUserResponse updateUser(String id, AdminUserRequest request);

    /**
     * Xóa user
     */
    void deleteUser(String id);

    /**
     * Toggle trạng thái active/ban của user
     */
    AdminUserResponse toggleUserStatus(String id);

    /**
     * Cập nhật roles cho user
     */
    AdminUserResponse updateUserRoles(String id, UpdateRolesRequest request);
}
