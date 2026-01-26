package com.shoppeclone.backend.admin.controller;

import com.shoppeclone.backend.admin.dto.request.AdminUserRequest;
import com.shoppeclone.backend.admin.dto.request.UpdateRolesRequest;
import com.shoppeclone.backend.admin.dto.response.AdminUserResponse;
import com.shoppeclone.backend.admin.dto.response.UserListResponse;
import com.shoppeclone.backend.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * GET /api/admin/users
     * Lấy danh sách users với pagination, search, filter
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserListResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role) {
        UserListResponse response = adminUserService.getAllUsers(page, size, search, role);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/users/{id}
     * Lấy chi tiết user theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable String id) {
        AdminUserResponse response = adminUserService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/admin/users
     * Tạo user mới
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponse> createUser(@RequestBody AdminUserRequest request) {
        AdminUserResponse response = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/admin/users/{id}
     * Cập nhật thông tin user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponse> updateUser(
            @PathVariable String id,
            @RequestBody AdminUserRequest request) {
        AdminUserResponse response = adminUserService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/admin/users/{id}
     * Xóa user
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String id) {
        adminUserService.deleteUser(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/admin/users/{id}/status
     * Toggle trạng thái active/ban của user
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponse> toggleUserStatus(@PathVariable String id) {
        AdminUserResponse response = adminUserService.toggleUserStatus(id);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/admin/users/{id}/roles
     * Cập nhật roles cho user
     */
    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponse> updateUserRoles(
            @PathVariable String id,
            @RequestBody UpdateRolesRequest request) {
        AdminUserResponse response = adminUserService.updateUserRoles(id, request);
        return ResponseEntity.ok(response);
    }
}
