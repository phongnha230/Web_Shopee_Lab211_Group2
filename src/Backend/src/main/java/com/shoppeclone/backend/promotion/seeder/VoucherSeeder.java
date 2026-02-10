package com.shoppeclone.backend.promotion.seeder;

import com.shoppeclone.backend.product.entity.Category;
import com.shoppeclone.backend.product.repository.CategoryRepository;
import com.shoppeclone.backend.promotion.entity.Voucher;
import com.shoppeclone.backend.promotion.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Order(3) // Run after ProductSeeder
public class VoucherSeeder implements CommandLineRunner {

        private final VoucherRepository voucherRepository;
        private final CategoryRepository categoryRepository;

        @Override
        public void run(String... args) throws Exception {
                // Always reset vouchers to ensure fresh data and correct types/dates
                voucherRepository.deleteAll();
                System.out.println(">>> Cleared existing vouchers.");

                System.out.println(">>> Seeding Vouchers...");
                List<Voucher> vouchers = new ArrayList<>();

                // === 1. Product Vouchers (with category restriction) ==

                // 10% OFF Electronics - only for Electronics category
                vouchers.add(createVoucher("ELECTRO10", "10% OFF Electronics", Voucher.VoucherType.PRODUCT,
                                Voucher.DiscountType.PERCENTAGE, new BigDecimal("10"),
                                new BigDecimal("100000"), 450, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59),
                                "Electronics"));

                // 20% OFF Fashion Items - only for Fashion category
                vouchers.add(createVoucher("FASHION20", "20% OFF Fashion Items", Voucher.VoucherType.PRODUCT,
                                Voucher.DiscountType.PERCENTAGE, new BigDecimal("20"),
                                new BigDecimal("50000"), 320, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59),
                                "Fashion"));

                // 50% OFF First Order - no category restriction (all products)
                vouchers.add(createVoucher("WELCOME50", "50% OFF First Order", Voucher.VoucherType.PRODUCT,
                                Voucher.DiscountType.PERCENTAGE, new BigDecimal("50"),
                                BigDecimal.ZERO, 999, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59),
                                (String) null));

                // 15% OFF Mobile Phones - only for Mobile & Gadgets category
                vouchers.add(createVoucher("MOBILE15", "15% OFF Mobile Phones", Voucher.VoucherType.PRODUCT,
                                Voucher.DiscountType.PERCENTAGE, new BigDecimal("15"),
                                new BigDecimal("200000"), 89, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59),
                                "Mobile & Gadgets"));

                // 30 OFF Sports Items - only for Sports category
                vouchers.add(createVoucher("SPORT30", "₫30k OFF Sports Items", Voucher.VoucherType.PRODUCT,
                                Voucher.DiscountType.FIXED_AMOUNT, new BigDecimal("30000"),
                                new BigDecimal("100000"), 156, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59),
                                "Sports"));

                // 25% OFF Home & Living - only for Home category
                vouchers.add(createVoucher("HOME25", "25% OFF Home & Living", Voucher.VoucherType.PRODUCT,
                                Voucher.DiscountType.PERCENTAGE, new BigDecimal("25"),
                                new BigDecimal("150000"), 234, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59),
                                "Home"));

                // 15% OFF Beauty - only for Beauty category
                vouchers.add(createVoucher("BEAUTY15", "15% OFF Beauty Products", Voucher.VoucherType.PRODUCT,
                                Voucher.DiscountType.PERCENTAGE, new BigDecimal("15"),
                                new BigDecimal("100000"), 180, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59),
                                "Beauty"));

                // 20% OFF Baby & Toys - only for Baby & Toys category
                vouchers.add(createVoucher("BABY20", "20% OFF Baby & Toys", Voucher.VoucherType.PRODUCT,
                                Voucher.DiscountType.PERCENTAGE, new BigDecimal("20"),
                                new BigDecimal("120000"), 200, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59),
                                "Baby & Toys"));

                // ₫25k OFF Food - only for Food category
                vouchers.add(createVoucher("FOOD25", "₫25k OFF Food Items", Voucher.VoucherType.PRODUCT,
                                Voucher.DiscountType.FIXED_AMOUNT, new BigDecimal("25000"),
                                new BigDecimal("80000"), 150, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59),
                                "Food"));

                // 25% OFF Books - only for Books category
                vouchers.add(createVoucher("BOOKS25", "25% OFF Books", Voucher.VoucherType.PRODUCT,
                                Voucher.DiscountType.PERCENTAGE, new BigDecimal("25"),
                                new BigDecimal("50000"), 300, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59),
                                "Books"));

                // === 2. Free Shipping Vouchers (no category restriction) ===

                // Free Shipping Voucher (₫15k OFF)
                vouchers.add(createVoucher("FREESHIP15", "Free Shipping Voucher (Max 15k)",
                                Voucher.VoucherType.SHIPPING,
                                Voucher.DiscountType.FIXED_AMOUNT, new BigDecimal("15000"),
                                new BigDecimal("50000"), 234, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59),
                                (String) null));

                // Super Freeship Voucher (₫30k OFF)
                vouchers.add(createVoucher("SUPER30", "Super Freeship Voucher (Max 30k)", Voucher.VoucherType.SHIPPING,
                                Voucher.DiscountType.FIXED_AMOUNT, new BigDecimal("30000"),
                                new BigDecimal("100000"), 89, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59),
                                (String) null));

                // Express Delivery Free (₫25k OFF)
                vouchers.add(createVoucher("EXPRESS25", "Express Delivery Free (Max 25k)", Voucher.VoucherType.SHIPPING,
                                Voucher.DiscountType.FIXED_AMOUNT, new BigDecimal("25000"),
                                new BigDecimal("75000"), 156, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59),
                                (String) null));

                // Weekend Freeship (₫20k OFF)
                vouchers.add(createVoucher("WEEKEND20", "Weekend Freeship (Max 20k)", Voucher.VoucherType.SHIPPING,
                                Voucher.DiscountType.FIXED_AMOUNT, new BigDecimal("20000"),
                                new BigDecimal("60000"), 45, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59),
                                (String) null));

                // New User Freeship (₫40k OFF)
                vouchers.add(createVoucher("NEWUSER40", "New User Freeship (Max 40k)", Voucher.VoucherType.SHIPPING,
                                Voucher.DiscountType.FIXED_AMOUNT, new BigDecimal("40000"),
                                BigDecimal.ZERO, 999, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59),
                                (String) null));

                // Flash Sale Freeship (₫50k OFF)
                vouchers.add(createVoucher("FLASH50", "Flash Sale Freeship (Max 50k)", Voucher.VoucherType.SHIPPING,
                                Voucher.DiscountType.FIXED_AMOUNT, new BigDecimal("50000"),
                                new BigDecimal("200000"), 12, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59),
                                (String) null));

                voucherRepository.saveAll(vouchers);
                System.out.println(">>> Vouchers seeded successfully!");
        }

        private Voucher createVoucher(String code, String description, Voucher.VoucherType voucherType,
                        Voucher.DiscountType type,
                        BigDecimal value, BigDecimal minSpend, int quantity, LocalDateTime expiry,
                        String categoryName) {
                Voucher v = new Voucher();
                v.setCode(code);
                v.setDescription(description);
                v.setType(voucherType);
                v.setDiscountType(type);
                v.setDiscountValue(value);
                v.setMinSpend(minSpend);
                v.setQuantity(quantity);
                v.setStartDate(LocalDateTime.now().minusDays(1)); // Started yesterday
                v.setEndDate(expiry);
                v.setActive(true);
                if (categoryName != null && !categoryName.isBlank()) {
                        Optional<Category> cat = categoryRepository.findByName(categoryName);
                        if (cat.isPresent()) {
                                v.setCategoryIds(List.of(cat.get().getId()));
                                v.setCategoryRestriction(categoryName);
                        }
                }
                return v;
        }
}
