package com.shoppeclone.backend.common.config;

import com.shoppeclone.backend.auth.model.Role;
import com.shoppeclone.backend.auth.repository.RoleRepository;
import com.shoppeclone.backend.payment.entity.PaymentMethod;
import com.shoppeclone.backend.payment.repository.PaymentMethodRepository;
import com.shoppeclone.backend.shipping.entity.ShippingProvider;
import com.shoppeclone.backend.shipping.repository.ShippingProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final ShippingProviderRepository shippingProviderRepository;

    @Override
    public void run(String... args) {
        // Seed Roles
        createRoleIfNotFound("ROLE_USER", "Regular User");
        createRoleIfNotFound("ROLE_ADMIN", "Administrator");
        createRoleIfNotFound("ROLE_SELLER", "Seller");
        createRoleIfNotFound("ROLE_SHIPPER", "Delivery Person");
        System.out.println("✅ Role verification and initialization completed!");

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
        createPaymentMethodIfNotFound("E_WALLET", "E-Wallet");
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