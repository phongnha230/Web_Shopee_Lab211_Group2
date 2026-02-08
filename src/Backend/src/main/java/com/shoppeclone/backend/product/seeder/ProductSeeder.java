package com.shoppeclone.backend.product.seeder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppeclone.backend.product.entity.Category;
import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.entity.ProductCategory;
import com.shoppeclone.backend.product.entity.ProductImage;
import com.shoppeclone.backend.product.entity.ProductStatus;
import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.product.repository.CategoryRepository;
import com.shoppeclone.backend.product.repository.ProductCategoryRepository;
import com.shoppeclone.backend.product.repository.ProductImageRepository;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import com.shoppeclone.backend.shop.entity.Shop;
import com.shoppeclone.backend.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ProductSeeder - Restores User Products from JSON
 */
@Component
@Order(3) // Run after User and Shop seeders
@RequiredArgsConstructor
@Slf4j
public class ProductSeeder implements CommandLineRunner {

        private final ProductRepository productRepository;
        private final ProductVariantRepository productVariantRepository;
        private final ProductImageRepository productImageRepository;
        private final ShopRepository shopRepository;
        private final CategoryRepository categoryRepository;
        private final ProductCategoryRepository productCategoryRepository;
        private final ObjectMapper objectMapper;

        @Override
        public void run(String... args) {
                log.info("🚀 Starting Product Seeder (Restoration Mode)...");

                try {
                        // 1. Find User Shop
                        String targetShopId = "demo-shop-1";
                        Optional<Shop> baribanaiShop = shopRepository.findByName("Baribanai");

                        if (baribanaiShop.isPresent()) {
                                targetShopId = baribanaiShop.get().getId();
                                log.info("✅ Found user shop 'Baribanai'! Target Shop ID: {}", targetShopId);
                        } else {
                                log.warn("⚠️ Shop 'Baribanai' not found. Using default ID '{}'.", targetShopId);
                        }

                        // 2. Load products from JSON
                        File file = new File("products.json");
                        if (file.exists()) {
                                log.info("📂 Found 'products.json'. Restoring products...");
                                List<ProductDTO> products = objectMapper.readValue(file,
                                                new TypeReference<List<ProductDTO>>() {
                                                });

                                for (ProductDTO dto : products) {
                                        // ALWAYS update/restore to ensure Shop ID link is valid
                                        restoreProduct(dto, targetShopId);
                                }
                                log.info("✅ Product restoration completed!");
                        } else {
                                log.warn("❌ 'products.json' not found! Skipping restoration.");
                        }

                } catch (Exception e) {
                        log.error("❌ Error seeding products: ", e);
                }
        }

<<<<<<< HEAD
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
=======
        private void restoreProduct(ProductDTO dto, String shopId) {
                // Save Product
>>>>>>> d16a99a33158b963ff04a20b28289efbe549223b
                Product product = new Product();
                product.setId(dto.getId());
                product.setShopId(shopId);
                product.setName(dto.getName());
                product.setDescription(dto.getDescription());
                // product.setPrice(dto.getPrice()); // Removed: Product entity uses variants
                // product.setStock(dto.getStock()); // Removed: Product entity uses variants
                product.setStatus(ProductStatus.ACTIVE);
                product.setCreatedAt(LocalDateTime.now());
                product.setUpdatedAt(LocalDateTime.now());
                productRepository.save(product);

                // Save Images
                if (dto.getImages() != null) {
                        for (int i = 0; i < dto.getImages().size(); i++) {
                                ProductImage img = new ProductImage();
                                img.setProductId(product.getId());
                                img.setImageUrl(dto.getImages().get(i));
                                img.setDisplayOrder(i);
                                productImageRepository.save(img);
                        }
                }

                // Save Variants (Default one if none provided)
                if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
                        for (VariantDTO v : dto.getVariants()) {
                                ProductVariant variant = new ProductVariant();
                                variant.setProductId(product.getId());
                                variant.setColor(v.getColor());
                                variant.setSize(v.getSize());
                                variant.setPrice(v.getPrice() != null ? v.getPrice() : dto.getPrice());
                                variant.setStock(v.getStock());
                                productVariantRepository.save(variant);
                        }
                } else {
                        // Create default variant
                        ProductVariant variant = new ProductVariant();
                        variant.setId(product.getId()); // Match ID for frontend
                        variant.setProductId(product.getId());
                        variant.setPrice(dto.getPrice());
                        variant.setStock(dto.getStock());
                        variant.setColor("Default");
                        productVariantRepository.save(variant);
                }

                // Link Category - Smart Detection
                String categoryToAssign = null;

                // First, try explicit category from JSON
                if (dto.getCategory() != null) {
                        categoryToAssign = dto.getCategory();
                } else if (dto.getCategories() != null && !dto.getCategories().isEmpty()) {
                        categoryToAssign = dto.getCategories().get(0);
                } else {
                        // Auto-detect category from product name
                        categoryToAssign = detectCategoryFromName(product.getName());
                }

                if (categoryToAssign != null) {
                        addCategory(product.getId(), categoryToAssign);
                }

                log.info("👉 Restored Product: {} → Category: {}", product.getName(), categoryToAssign);
        }

        /**
         * Detect category from product name using keyword matching
         */
        private String detectCategoryFromName(String productName) {
                if (productName == null)
                        return "Electronics"; // Default fallback

                String lowerName = productName.toLowerCase();

                // Fashion keywords
                if (lowerName.contains("shoe") || lowerName.contains("sneaker") ||
                                lowerName.contains("nike") || lowerName.contains("adidas") ||
                                lowerName.contains("shirt") || lowerName.contains("dress") ||
                                lowerName.contains("jacket") || lowerName.contains("pant") ||
                                lowerName.contains("jean") || lowerName.contains("clothing")) {
                        return "Fashion";
                }

                // Mobile & Gadgets keywords
                if (lowerName.contains("phone") || lowerName.contains("iphone") ||
                                lowerName.contains("samsung") || lowerName.contains("xiaomi") ||
                                lowerName.contains("mobile") || lowerName.contains("smartphone") ||
                                lowerName.contains("tablet") || lowerName.contains("ipad")) {
                        return "Mobile & Gadgets";
                }

                // Electronics keywords
                if (lowerName.contains("headphone") || lowerName.contains("earphone") ||
                                lowerName.contains("speaker") || lowerName.contains("laptop") ||
                                lowerName.contains("computer") || lowerName.contains("camera") ||
                                lowerName.contains("tv") || lowerName.contains("monitor") ||
                                lowerName.contains("keyboard") || lowerName.contains("mouse")) {
                        return "Electronics";
                }

                // Home keywords
                if (lowerName.contains("chair") || lowerName.contains("table") ||
                                lowerName.contains("sofa") || lowerName.contains("bed") ||
                                lowerName.contains("furniture") || lowerName.contains("lamp") ||
                                lowerName.contains("home") || lowerName.contains("kitchen")) {
                        return "Home";
                }

                // Sports keywords
                if (lowerName.contains("sport") || lowerName.contains("running") ||
                                lowerName.contains("gym") || lowerName.contains("fitness") ||
                                lowerName.contains("yoga") || lowerName.contains("ball") ||
                                lowerName.contains("exercise")) {
                        return "Sports";
                }

                // Beauty keywords
                if (lowerName.contains("beauty") || lowerName.contains("makeup") ||
                                lowerName.contains("cosmetic") || lowerName.contains("skincare") ||
                                lowerName.contains("perfume") || lowerName.contains("lipstick")) {
                        return "Beauty";
                }

                // Watch keywords (Fashion subcategory)
                if (lowerName.contains("watch") || lowerName.contains("clock")) {
                        return "Fashion";
                }

                // Books keywords
                if (lowerName.contains("book") || lowerName.contains("novel") ||
                                lowerName.contains("magazine") || lowerName.contains("reading")) {
                        return "Books";
                }

                // Baby & Toys keywords
                if (lowerName.contains("baby") || lowerName.contains("toy") ||
                                lowerName.contains("kid") || lowerName.contains("child")) {
                        return "Baby & Toys";
                }

                // Food keywords
                if (lowerName.contains("food") || lowerName.contains("snack") ||
                                lowerName.contains("drink") || lowerName.contains("coffee")) {
                        return "Food";
                }

                // Default to Electronics if no match
                return "Electronics";
        }

        private void addCategory(String productId, String categoryName) {
                Optional<Category> categoryOpt = categoryRepository.findByName(categoryName);
                if (categoryOpt.isPresent()) {
                        ProductCategory pc = new ProductCategory();
                        pc.setProductId(productId);
                        pc.setCategoryId(categoryOpt.get().getId());
                        productCategoryRepository.save(pc);
                }
        }

        // DTO Helper Classes for JSON Deserialization
        @lombok.Data
        @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
        static class ProductDTO {
                private String id;
                private String name;
                private String description;
                private BigDecimal price;
                private int stock;
                private List<String> images;
                private List<VariantDTO> variants;
                private String category;
                private List<String> categories;
        }

        @lombok.Data
        static class VariantDTO {
                private String color;
                private String size;
                private BigDecimal price;
                private int stock;
        }
}
