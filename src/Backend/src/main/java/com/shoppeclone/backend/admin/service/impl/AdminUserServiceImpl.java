package com.shoppeclone.backend.admin.service.impl;

import com.shoppeclone.backend.admin.dto.request.AdminUserRequest;
import com.shoppeclone.backend.admin.dto.request.UpdateRolesRequest;
import com.shoppeclone.backend.admin.dto.response.AdminUserResponse;
import com.shoppeclone.backend.admin.dto.response.UserListResponse;
import com.shoppeclone.backend.admin.service.AdminUserService;
import com.shoppeclone.backend.auth.model.Role;
import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.RoleRepository;
import com.shoppeclone.backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;

    @Override
    public UserListResponse getAllUsers(int page, int size, String search, String role) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Query query = new Query();

        // Search by email or fullName
        if (search != null && !search.isEmpty()) {
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("email").regex(search, "i"),
                    Criteria.where("fullName").regex(search, "i"));
            query.addCriteria(searchCriteria);
        }

        // Filter by role
        if (role != null && !role.isEmpty()) {
            query.addCriteria(Criteria.where("roles.name").is(role));
        }

        query.with(pageable);

        List<User> users = mongoTemplate.find(query, User.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), User.class);

        Page<User> userPage = PageableExecutionUtils.getPage(users, pageable, () -> total);

        List<AdminUserResponse> userResponses = users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new UserListResponse(
                userResponses,
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.getNumber(),
                userPage.getSize());
    }

    @Override
    public AdminUserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Override
    public AdminUserResponse createUser(AdminUserRequest request) {
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        // Mã hóa password nếu có
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Set roles
        Set<Role> roles = getRolesFromNames(request.getRoles());
        user.setRoles(roles);

        user.setActive(request.getActive() != null ? request.getActive() : true);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Override
    public AdminUserResponse updateUser(String id, AdminUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Cập nhật thông tin
        if (request.getEmail() != null) {
            // Kiểm tra email mới có trùng với user khác không
            if (!user.getEmail().equals(request.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        // Cập nhật password nếu có
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Cập nhật roles nếu có
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = getRolesFromNames(request.getRoles());
            user.setRoles(roles);
        }

        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Không cho phép xóa admin cuối cùng
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (isAdmin) {
            long adminCount = userRepository.countByRolesName("ROLE_ADMIN");

            if (adminCount <= 1) {
                throw new RuntimeException("Cannot delete the last admin user");
            }
        }

        userRepository.deleteById(id);
    }

    @Override
    public AdminUserResponse toggleUserStatus(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setActive(!user.isActive());
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    public AdminUserResponse updateUserRoles(String id, UpdateRolesRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        Set<Role> roles = getRolesFromNames(request.getRoles());
        user.setRoles(roles);
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    // ==================== HELPER METHODS ====================

    private AdminUserResponse mapToResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                roleNames,
                user.isEmailVerified(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    private Set<Role> getRolesFromNames(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            // Default role
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Role ROLE_USER not found"));
            return Set.of(userRole);
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            roles.add(role);
        }
        return roles;
    }
}
