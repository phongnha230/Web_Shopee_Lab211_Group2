package com.shoppeclone.backend.common.config;

import com.shoppeclone.backend.auth.model.Role;
import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.RoleRepository;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.shop.entity.Shop;
import com.shoppeclone.backend.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RoleFixer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final RoleRepository roleRepository;
    private final com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleCampaignRepository flashSaleCampaignRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🔧 RoleFixer (Auto-Fix): Starting permission check for Shop Owners...");

        // Ensure ROLE_SELLER exists
        Role sellerRole = roleRepository.findByName("ROLE_SELLER")
                .orElseGet(() -> {
                    System.out.println("⚠️ ROLE_SELLER not found. Auto-creating ROLE_SELLER...");
                    Role newRole = new Role();
                    newRole.setName("ROLE_SELLER");
                    newRole.setDescription("Seller Role - Grants access to Seller Dashboard & Flash Sales");
                    return roleRepository.save(newRole);
                });

        List<Shop> allShops = shopRepository.findAll();
        int fixedCount = 0;

        for (Shop shop : allShops) {
            if (shop.getOwnerId() != null) {
                // Find user by ID (ownerId is String)
                Optional<User> userOpt = userRepository.findById(shop.getOwnerId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    // Check if user already has ROLE_SELLER
                    boolean hasSellerRole = user.getRoles().stream()
                            .anyMatch(r -> "ROLE_SELLER".equals(r.getName()));

                    if (!hasSellerRole) {
                        System.out.println("🛠️ RoleFixer: Granting ROLE_SELLER to user: " + user.getEmail()
                                + " (Shop: " + shop.getName() + ")");
                        user.getRoles().add(sellerRole);
                        userRepository.save(user); // Save updated user
                        fixedCount++;
                    }
                }
            }
        }

        if (fixedCount > 0) {
            System.out.println("✅ RoleFixer: Successfully updated " + fixedCount + " shop owners with ROLE_SELLER.");
        } else {
            System.out.println("✅ RoleFixer: All shop owners already have correct permissions.");
        }

        // --- DEBUG FLASH SALE CAMPAIGNS ---
        System.out.println("\n📊 DEBUG: Inspecting Flash Sale Campaigns...");
        List<com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleCampaign> campaigns = flashSaleCampaignRepository
                .findAll();
        if (campaigns.isEmpty()) {
            System.out.println("❌ DEBUG: No Flash Sale Campaigns found in database!");
        } else {
            for (com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleCampaign c : campaigns) {
                System.out.println("👉 Campaign ID: " + c.getId());
                System.out.println("   Name: " + c.getName());
                System.out.println("   Status: [" + c.getStatus() + "]");
                System.out.println("   Start Date: " + c.getStartDate());
                System.out.println("   Reg Deadline: " + c.getRegistrationDeadline());
                System.out.println("   Reg Open? " + (c.getStatus().equals("REGISTRATION_OPEN") ? "YES" : "NO"));
                System.out.println("-------------------------------------------------");
            }
        }
        System.out.println("📊 DEBUG: Inspection Complete.\n");
    }
}
