package com.shoppeclone.backend.common.config;

import com.shoppeclone.backend.auth.model.Role;
import com.shoppeclone.backend.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        createRoleIfNotFound("ROLE_USER", "Regular User");
        createRoleIfNotFound("ROLE_ADMIN", "Administrator");
        createRoleIfNotFound("ROLE_SELLER", "Seller");

        System.out.println("✅ Role verification and initialization completed!");
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