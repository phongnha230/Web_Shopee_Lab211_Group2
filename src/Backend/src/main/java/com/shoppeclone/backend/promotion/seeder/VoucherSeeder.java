package com.shoppeclone.backend.promotion.seeder;

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

@Component
@RequiredArgsConstructor
@Order(3) // Run after ProductSeeder
public class VoucherSeeder implements CommandLineRunner {

        private final VoucherRepository voucherRepository;

        @Override
        public void run(String... args) throws Exception {
                // Always reset vouchers to ensure fresh data and correct types/dates
                voucherRepository.deleteAll();
                System.out.println(">>> Cleared existing vouchers.");

                System.out.println(">>> Seeding Vouchers...");
                List<Voucher> vouchers = new ArrayList<>();

                // === 1. Product Vouchers ==

                // 10% OFF Electronics
                vouchers.add(createVoucher("ELECTRO10", "10% OFF Electronics", Voucher.VoucherType.PRODUCT,
                                Voucher.DiscountType.PERCENTAGE, new BigDecimal("10"),
                                new BigDecimal("100000"), 450, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59)));

                // 20% OFF Fashion Items
                vouchers.add(createVoucher("FASHION20", "20% OFF Fashion Items", Voucher.VoucherType.PRODUCT,
                                Voucher.DiscountType.PERCENTAGE, new BigDecimal("20"),
                                new BigDecimal("50000"), 320, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59)));

                // 50% OFF First Order
                vouchers.add(createVoucher("WELCOME50", "50% OFF First Order", Voucher.VoucherType.PRODUCT,
                                Voucher.DiscountType.PERCENTAGE, new BigDecimal("50"),
                                BigDecimal.ZERO, 999, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59)));

                // 15% OFF Mobile Phones
                vouchers.add(createVoucher("MOBILE15", "15% OFF Mobile Phones", Voucher.VoucherType.PRODUCT,
                                Voucher.DiscountType.PERCENTAGE, new BigDecimal("15"),
                                new BigDecimal("200000"), 89, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59)));

                // 30 OFF Sports Items (Fixed Amount)
                vouchers.add(createVoucher("SPORT30", "₫30k OFF Sports Items", Voucher.VoucherType.PRODUCT,
                                Voucher.DiscountType.FIXED_AMOUNT, new BigDecimal("30000"),
                                new BigDecimal("100000"), 156, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59)));

                // 25% OFF Home & Living
                vouchers.add(createVoucher("HOME25", "25% OFF Home & Living", Voucher.VoucherType.PRODUCT,
                                Voucher.DiscountType.PERCENTAGE, new BigDecimal("25"),
                                new BigDecimal("150000"), 234, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59)));

                // === 2. Free Shipping Vouchers ===

                // Free Shipping Voucher (₫15k OFF)
                vouchers.add(createVoucher("FREESHIP15", "Free Shipping Voucher (Max 15k)",
                                Voucher.VoucherType.SHIPPING,
                                Voucher.DiscountType.FIXED_AMOUNT, new BigDecimal("15000"),
                                new BigDecimal("50000"), 234, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59)));

                // Super Freeship Voucher (₫30k OFF)
                vouchers.add(createVoucher("SUPER30", "Super Freeship Voucher (Max 30k)", Voucher.VoucherType.SHIPPING,
                                Voucher.DiscountType.FIXED_AMOUNT, new BigDecimal("30000"),
                                new BigDecimal("100000"), 89, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59)));

                // Express Delivery Free (₫25k OFF)
                vouchers.add(createVoucher("EXPRESS25", "Express Delivery Free (Max 25k)", Voucher.VoucherType.SHIPPING,
                                Voucher.DiscountType.FIXED_AMOUNT, new BigDecimal("25000"),
                                new BigDecimal("75000"), 156, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59)));

                // Weekend Freeship (₫20k OFF)
                vouchers.add(createVoucher("WEEKEND20", "Weekend Freeship (Max 20k)", Voucher.VoucherType.SHIPPING,
                                Voucher.DiscountType.FIXED_AMOUNT, new BigDecimal("20000"),
                                new BigDecimal("60000"), 45, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59)));

                // New User Freeship (₫40k OFF)
                vouchers.add(createVoucher("NEWUSER40", "New User Freeship (Max 40k)", Voucher.VoucherType.SHIPPING,
                                Voucher.DiscountType.FIXED_AMOUNT, new BigDecimal("40000"),
                                BigDecimal.ZERO, 999, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59)));

                // Flash Sale Freeship (₫50k OFF)
                vouchers.add(createVoucher("FLASH50", "Flash Sale Freeship (Max 50k)", Voucher.VoucherType.SHIPPING,
                                Voucher.DiscountType.FIXED_AMOUNT, new BigDecimal("50000"),
                                new BigDecimal("200000"), 12, LocalDateTime.of(2026, Month.DECEMBER, 31, 23, 59)));

                voucherRepository.saveAll(vouchers);
                System.out.println(">>> Vouchers seeded successfully!");
        }

        private Voucher createVoucher(String code, String description, Voucher.VoucherType voucherType,
                        Voucher.DiscountType type,
                        BigDecimal value, BigDecimal minSpend, int quantity, LocalDateTime expiry) {
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
                return v;
        }
}
