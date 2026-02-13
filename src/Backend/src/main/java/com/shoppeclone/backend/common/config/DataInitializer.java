package com.shoppeclone.backend.common.config;

import com.shoppeclone.backend.auth.model.Role;
import com.shoppeclone.backend.auth.repository.RoleRepository;
import com.shoppeclone.backend.product.entity.Category;
import com.shoppeclone.backend.product.repository.CategoryRepository;
import com.shoppeclone.backend.payment.entity.PaymentMethod;
import com.shoppeclone.backend.payment.repository.PaymentMethodRepository;
import com.shoppeclone.backend.shipping.entity.ShippingProvider;
import com.shoppeclone.backend.shipping.repository.ShippingProviderRepository;
import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.promotion.flashsale.service.FlashSaleService;
import com.shoppeclone.backend.promotion.flashsale.dto.FlashSaleCampaignRequest;
import com.shoppeclone.backend.promotion.flashsale.dto.FlashSaleSlotRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final ShippingProviderRepository shippingProviderRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FlashSaleService flashSaleService;

    @Override
    public void run(String... args) {
        // Seed Roles
        createRoleIfNotFound("ROLE_USER", "Regular User");
        createRoleIfNotFound("ROLE_ADMIN", "Administrator");
        createRoleIfNotFound("ROLE_SELLER", "Seller");
        System.out.println("✅ Role verification and initialization completed!");

        seedAdminUser();
        seedAdminUser();
        seedCategories();
        // seedFlashSaleCampaign();
    }

    private void seedFlashSaleCampaign() {
        try {
            if (flashSaleService.getAllCampaigns().stream()
                    .noneMatch(c -> c.getStatus().equals("OPEN") || c.getStatus().equals("ONGOING"))) {
                System.out.println("⚡ Seeding Default Flash Sale Campaign...");

                // Create Campaign
                FlashSaleCampaignRequest campaignReq = new FlashSaleCampaignRequest();
                campaignReq.setName("Flash Sale T2 (Auto)");
                campaignReq.setDescription("Automated Daily Flash Sale");
                campaignReq.setStartDate(LocalDateTime.now());
                campaignReq.setEndDate(LocalDateTime.now().plusHours(48));
                campaignReq.setRegistrationDeadline(LocalDateTime.now().plusHours(24));
                campaignReq.setApprovalDeadline(LocalDateTime.now().plusHours(24));
                campaignReq.setMinDiscountPercentage(5);
                campaignReq.setMinStockPerProduct(5);

                var campaign = flashSaleService.createCampaign(campaignReq);

                // Create Slot
                FlashSaleSlotRequest slotReq = new FlashSaleSlotRequest();
                slotReq.setCampaignId(campaign.getId());
                slotReq.setStartTime(LocalDateTime.now().plusMinutes(30));
                slotReq.setEndTime(LocalDateTime.now().plusHours(2));

                flashSaleService.createSlot(slotReq);
                System.out.println("✅ Seeded Campaign: " + campaign.getName());
            } else {
                System.out.println("✅ Active Flash Sale Campaign already exists.");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to seed Flash Sale: " + e.getMessage());
        }
    }

    private void seedAdminUser() {
        // 1. Promote "tuongvy22102004@gmail.com" to ADMIN
        String realAdminEmail = "tuongvy22102004@gmail.com";
        userRepository.findByEmail(realAdminEmail).ifPresentOrElse(
                user -> {
                    com.shoppeclone.backend.auth.model.Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

                    boolean alreadyHasRole = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
                    if (!alreadyHasRole) {
                        user.getRoles().add(adminRole);
                        userRepository.save(user);
                        System.out.println("👉 Promoted " + realAdminEmail + " to ADMIN.");
                    } else {
                        System.out.println("✅ " + realAdminEmail + " is already an ADMIN.");
                    }
                },
                () -> System.out.println("⚠️ User " + realAdminEmail + " not found. Please register first!"));

        // 2. Remove temporary admin "admin@shopee.com"
        userRepository.findByEmail("admin@shopee.com").ifPresent(user -> {
            userRepository.delete(user);
            System.out.println("❌ Removed temporary admin: admin@shopee.com");
        });
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
        // Seed Payment Methods
        seedPaymentMethods();
        System.out.println("✅ Payment methods initialization completed!");

        // Seed Shipping Providers
        seedShippingProviders();
        System.out.println("✅ Shipping providers initialization completed!");
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

    private void seedPaymentMethods() {
        createPaymentMethodIfNotFound("COD", "Cash on Delivery");
        createPaymentMethodIfNotFound("CARD", "Credit/Debit Card");
        createPaymentMethodIfNotFound("CREDIT_CARD", "Credit/Debit Card");
        createPaymentMethodIfNotFound("E_WALLET", "E-Wallet");
        createPaymentMethodIfNotFound("MOMO", "MoMo");
        createPaymentMethodIfNotFound("VNPAY", "VNPAY");
    }

    private void createPaymentMethodIfNotFound(String code, String name) {
        if (paymentMethodRepository.findByCode(code).isEmpty()) {
            PaymentMethod method = new PaymentMethod();
            method.setCode(code);
            method.setName(name);
            paymentMethodRepository.save(method);
            System.out.println("👉 Created payment method: " + code + " - " + name);
        } else {
            System.out.println("✅ Payment method already exists: " + code);
        }
    }

    private void seedShippingProviders() {
        createShippingProviderIfNotFound("standard", "Standard Delivery", "https://api.standard-shipping.com");
        createShippingProviderIfNotFound("express", "Express Shipping", "https://api.express-shipping.com");
    }

    private void createShippingProviderIfNotFound(String id, String name, String apiEndpoint) {
        if (shippingProviderRepository.findById(id).isEmpty()) {
            ShippingProvider provider = new ShippingProvider();
            provider.setId(id);
            provider.setName(name);
            provider.setApiEndpoint(apiEndpoint);
            shippingProviderRepository.save(provider);
            System.out.println("👉 Created shipping provider: " + id + " - " + name);
        } else {
            System.out.println("✅ Shipping provider already exists: " + id);
        }
    }
}