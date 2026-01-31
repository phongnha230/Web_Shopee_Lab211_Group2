package com.shoppeclone.backend.common.config;

import com.shoppeclone.backend.auth.model.Role;
import com.shoppeclone.backend.auth.repository.RoleRepository;
import com.shoppeclone.backend.product.entity.Category;
import com.shoppeclone.backend.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        createRoleIfNotFound("ROLE_USER", "Regular User");
        createRoleIfNotFound("ROLE_ADMIN", "Administrator");
        createRoleIfNotFound("ROLE_SELLER", "Seller");

        System.out.println("✅ Role verification and initialization completed!");

        seedCategories();
    }

    private void seedCategories() {
        List<String> allowedCategories = List.of(
                "Fashion",
                "Electronics",
                "Mobile & Gadgets",
                "Home",
                "Beauty",
                "Baby & Toys",
                "Sports",
                "Food",
                "Books");

        // 1. Remove categories NOT in the allowed list
        List<Category> existingCategories = categoryRepository.findAll();
        for (Category cat : existingCategories) {
            if (!allowedCategories.contains(cat.getName())) {
                categoryRepository.delete(cat);
                System.out.println("❌ Removed deprecated category: " + cat.getName());
            }
        }

        // 2. Add missing categories
        for (String name : allowedCategories) {
            if (!categoryRepository.existsByName(name)) {
                Category cat = new Category();
                cat.setName(name);
                cat.setCreatedAt(LocalDateTime.now());
                cat.setUpdatedAt(LocalDateTime.now());
                categoryRepository.save(cat);
                System.out.println("👉 Seeded Category: " + name);
            }
        }
    }

    private void createRoleIfNotFound(String roleName, String description) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            role.setDescription(description);
            roleRepository.save(role);
            System.out.println("👉 Created new role: " + roleName);
        } else {
            System.out.println("✅ Role already exists: " + roleName);
        }
    }
}