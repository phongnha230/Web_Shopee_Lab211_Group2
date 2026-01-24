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
        // Kiểm tra nếu chưa có role nào thì tạo
        if (roleRepository.count() == 0) {
            
            // Role USER
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            userRole.setDescription("Người dùng thông thường");
            roleRepository.save(userRole);
            
            // Role ADMIN
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            adminRole.setDescription("Quản trị viên");
            roleRepository.save(adminRole);
            
            // Role SELLER
            Role sellerRole = new Role();
            sellerRole.setName("ROLE_SELLER");
            sellerRole.setDescription("Người bán hàng");
            roleRepository.save(sellerRole);
            
            System.out.println("✅ Đã khởi tạo các Role thành công!");
        } else {
            System.out.println("✅ Roles đã tồn tại, bỏ qua khởi tạo.");
        }
    }
}