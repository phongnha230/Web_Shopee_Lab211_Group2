package com.shoppeclone.backend.product.seeder;

import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.entity.ProductImage;
import com.shoppeclone.backend.product.entity.ProductStatus;
import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.product.repository.ProductImageRepository;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * ProductSeeder - Synced with Frontend MOCK_PRODUCTS
 * Creates same products as frontend for consistent UI/UX
 */
@Component
@Order(3) // Run after User and Shop seeders
@RequiredArgsConstructor
@Slf4j
public class ProductSeeder implements CommandLineRunner {

        private final ProductRepository productRepository;
        private final ProductVariantRepository productVariantRepository;
        private final ProductImageRepository productImageRepository;

        @Override
        public void run(String... args) {
                // if (productRepository.count() > 0) {
                // log.info("Products already seeded, skipping...");
                // return;
                // }

                // FORCE RESET DATA TO APPLY VARIANT ID FIX
                log.info("♻️ Force clearing product data for re-seeding...");
                productRepository.deleteAll();
                productVariantRepository.deleteAll();
                productImageRepository.deleteAll();

                log.info("Seeding products from frontend MOCK_PRODUCTS data...");

                try {
                        String demoShopId = "demo-shop-1";

                        // Flash Sale Items (IDs: 1-6)
                        createProduct1_Headphones(demoShopId);
                        createProduct2_UrbanSneakers(demoShopId);
                        createProduct3_Watch(demoShopId);
                        createProduct4_RunningSportShoes(demoShopId);
                        createProduct5_SmartPhone(demoShopId);
                        createProduct6_CompactHeadphones(demoShopId);

                        // Daily Discover Items (IDs: 101-106)
                        createProduct101_NikonCamera(demoShopId);
                        createProduct102_NikeAirMax(demoShopId);
                        createProduct103_WaterBottle1L(demoShopId);
                        createProduct104_BlueWaterBottle(demoShopId);
                        createProduct105_ModernChair(demoShopId);
                        createProduct106_CanvasSneakers(demoShopId);

                        log.info("✅ Products seeded successfully! (12 products synced from frontend)");
                } catch (Exception e) {
                        log.error("❌ Error seeding products: ", e);
                }
        }

        // === FLASH SALE ITEMS ===

        private void createProduct1_Headphones(String shopId) {
                Product product = createProductBase("1", shopId,
                                "Wireless Headphones - Premium Bass",
                                "Experience sound like never before with these premium wireless headphones. Noise cancellation included.");

                addImages(product.getId(), Arrays.asList(
                                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&h=800&fit=crop",
                                "https://images.unsplash.com/photo-1484704849700-f032a568e944?w=800&h=800&fit=crop",
                                "https://images.unsplash.com/photo-1524678606372-571d751b8d57?w=800&h=800&fit=crop"));

                addColorVariants(product.getId(), new String[] { "Black", "Silver", "Red" },
                                new BigDecimal("99000"), new int[] { 156, 89, 45 });
        }

        private void createProduct2_UrbanSneakers(String shopId) {
                Product product = createProductBase("2", shopId,
                                "Urban Sneakers - High Comfort",
                                "Top urban design sneakers. Perfect for running and casual wear.");

                addImages(product.getId(), Arrays.asList(
                                "https://images.unsplash.com/photo-1491553895911-0055eca6402d?w=800&h=800&fit=crop",
                                "https://images.unsplash.com/photo-1600269452121-4f2416e55c28?w=800&h=800&fit=crop"));

                addColorSizeVariants(product.getId(),
                                new String[] { "White/Red", "Black" },
                                new String[] { "40", "41", "42", "43" },
                                new BigDecimal("102000"), 89);
        }

        private void createProduct3_Watch(String shopId) {
                Product product = createProductBase("3", shopId,
                                "Classic Wrist Watch",
                                "Timeless elegance on your wrist.");

                addImages(product.getId(), List.of(
                                "https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=800&h=800&fit=crop"));

                addColorVariants(product.getId(), new String[] { "Gold", "Silver" },
                                new BigDecimal("15000"), new int[] { 44, 44 });
        }

        private void createProduct4_RunningSportShoes(String shopId) {
                Product product = createProductBase("4", shopId,
                                "Running Sport Shoes",
                                "Professional grade running shoes.");

                addImages(product.getId(), List.of(
                                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&h=800&fit=crop"));

                addColorSizeVariants(product.getId(),
                                new String[] { "Red", "Blue" },
                                new String[] { "38", "39", "40", "41" },
                                new BigDecimal("120000"), 200);
        }

        private void createProduct5_SmartPhone(String shopId) {
                Product product = createProductBase("5", shopId,
                                "Smart Phone 12 Pro",
                                "Latest smartphone technology.");

                addImages(product.getId(), List.of(
                                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&h=800&fit=crop"));

                // Storage as "size"
                addColorSizeVariants(product.getId(),
                                new String[] { "Graphite", "Silver" },
                                new String[] { "128GB", "256GB" },
                                new BigDecimal("19999000"), 12);
        }

        private void createProduct6_CompactHeadphones(String shopId) {
                Product product = createProductBase("6", shopId,
                                "Compact Headphones",
                                "Compact and powerful.");

                addImages(product.getId(), List.of(
                                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&h=800&fit=crop"));

                addColorVariants(product.getId(), new String[] { "Black" },
                                new BigDecimal("45000"), new int[] { 330 });
        }

        // === DAILY DISCOVER ITEMS ===

        private void createProduct101_NikonCamera(String shopId) {
                Product product = createProductBase("101", shopId,
                                "Nikon D3500 DSLR Camera",
                                "Capture moments with stunning clarity using the Nikon D3500.");

                addImages(product.getId(), Arrays.asList(
                                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&h=800&fit=crop",
                                "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800&h=800&fit=crop"));

                addColorVariants(product.getId(), new String[] { "Black" },
                                new BigDecimal("3600000"), new int[] { 5 });
        }

        private void createProduct102_NikeAirMax(String shopId) {
                Product product = createProductBase("102", shopId,
                                "Nike Air Max Classic Red Shoes Original",
                                "Classic Nike Air Max running shoes. Lightweight and comfortable for everyday wear.");

                addImages(product.getId(), Arrays.asList(
                                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&h=800&fit=crop",
                                "https://images.unsplash.com/photo-1600269452121-4f2416e55c28?w=800&h=800&fit=crop"));

                addColorSizeVariants(product.getId(),
                                new String[] { "Red", "Black", "White" },
                                new String[] { "38", "39", "40", "41", "42", "43" },
                                new BigDecimal("210000"), 540);
        }

        private void createProduct103_WaterBottle1L(String shopId) {
                Product product = createProductBase("103", shopId,
                                "Sport Water Bottle 1L",
                                "Stay hydrated.");

                addImages(product.getId(), List.of(
                                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=400&fit=crop"));

                addColorSizeVariants(product.getId(),
                                new String[] { "Silver" },
                                new String[] { "1L" },
                                new BigDecimal("16000"), 980);
        }

        private void createProduct104_BlueWaterBottle(String shopId) {
                Product product = createProductBase("104", shopId,
                                "Blue Water Bottle",
                                "Premium quality.");

                addImages(product.getId(), List.of(
                                "https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=400&h=400&fit=crop"));

                addColorSizeVariants(product.getId(),
                                new String[] { "Blue" },
                                new String[] { "500ml" },
                                new BigDecimal("14500"), 760);
        }

        private void createProduct105_ModernChair(String shopId) {
                Product product = createProductBase("105", shopId,
                                "Modern Chair",
                                "Comfortable modern chair.");

                addImages(product.getId(), List.of(
                                "https://images.unsplash.com/photo-1587563871167-1ee9c731aefb?w=800&h=800&fit=crop"));

                addColorVariants(product.getId(), new String[] { "Brown" },
                                new BigDecimal("250000"), new int[] { 120 });
        }

        private void createProduct106_CanvasSneakers(String shopId) {
                Product product = createProductBase("106", shopId,
                                "Canvas Sneakers",
                                "Casual everyday wear.");

                addImages(product.getId(), List.of(
                                "https://images.unsplash.com/photo-1491553895911-0055eca6402d?w=800&h=800&fit=crop"));

                addColorSizeVariants(product.getId(),
                                new String[] { "White" },
                                new String[] { "38", "39" },
                                new BigDecimal("180000"), 230);
        }

        // === HELPER METHODS ===

        private Product createProductBase(String id, String shopId, String name, String description) {
                Product product = new Product();
                product.setId(id);
                product.setShopId(shopId);
                product.setName(name);
                product.setDescription(description);
                product.setStatus(ProductStatus.ACTIVE);
                product.setCreatedAt(LocalDateTime.now());
                product.setUpdatedAt(LocalDateTime.now());
                return productRepository.save(product);
        }

        private void addImages(String productId, List<String> imageUrls) {
                for (int i = 0; i < imageUrls.size(); i++) {
                        ProductImage img = new ProductImage();
                        img.setProductId(productId);
                        img.setImageUrl(imageUrls.get(i));
                        img.setDisplayOrder(i);
                        img.setCreatedAt(LocalDateTime.now());
                        productImageRepository.save(img);
                }
        }

        private void addColorVariants(String productId, String[] colors, BigDecimal price, int[] stocks) {
                for (int i = 0; i < colors.length; i++) {
                        ProductVariant variant = new ProductVariant();
                        // IMPORTANT: Set first variant ID = product ID for frontend compatibility
                        if (i == 0) {
                                variant.setId(productId);
                        }
                        variant.setProductId(productId);
                        variant.setColor(colors[i]);
                        variant.setPrice(price);
                        variant.setStock(stocks[i]);
                        variant.setCreatedAt(LocalDateTime.now());
                        variant.setUpdatedAt(LocalDateTime.now());
                        productVariantRepository.save(variant);
                }
        }

        private void addColorSizeVariants(String productId, String[] colors, String[] sizes, BigDecimal price,
                        int stockPerVariant) {
                boolean isFirst = true;
                for (String color : colors) {
                        for (String size : sizes) {
                                ProductVariant variant = new ProductVariant();
                                // IMPORTANT: Set first variant ID = product ID for frontend compatibility
                                if (isFirst) {
                                        variant.setId(productId);
                                        isFirst = false;
                                }
                                variant.setProductId(productId);
                                variant.setColor(color);
                                variant.setSize(size);
                                variant.setPrice(price);
                                variant.setStock(stockPerVariant);
                                variant.setCreatedAt(LocalDateTime.now());
                                variant.setUpdatedAt(LocalDateTime.now());
                                productVariantRepository.save(variant);
                        }
                }
        }
}
