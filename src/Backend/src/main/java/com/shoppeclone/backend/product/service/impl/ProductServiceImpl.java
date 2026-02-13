package com.shoppeclone.backend.product.service.impl;

import com.shoppeclone.backend.product.dto.request.CreateProductRequest;
import com.shoppeclone.backend.product.dto.request.CreateProductVariantRequest;
import com.shoppeclone.backend.product.dto.request.UpdateProductRequest;
import com.shoppeclone.backend.product.dto.response.ProductResponse;
import com.shoppeclone.backend.product.dto.response.ProductVariantResponse;
import com.shoppeclone.backend.product.entity.*;
import com.shoppeclone.backend.product.repository.*;
import com.shoppeclone.backend.product.service.ProductService;
import com.shoppeclone.backend.product.util.CategoryDetectionUtil;
import com.shoppeclone.backend.promotion.flashsale.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final FlashSaleService flashSaleService;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setShopId(request.getShopId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setIsFlashSale(request.getIsFlashSale() != null ? request.getIsFlashSale() : false);
        product.setFlashSalePrice(request.getFlashSalePrice());
        product.setFlashSaleStock(request.getFlashSaleStock());

        Product savedProduct = productRepository.save(product);
        boolean hasVariants = false;

        // 1. Save Variants
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            hasVariants = true;
            for (CreateProductVariantRequest vReq : request.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProductId(savedProduct.getId());
                variant.setSize(vReq.getSize());
                variant.setColor(vReq.getColor());
                variant.setPrice(vReq.getPrice());
                variant.setStock(vReq.getStock());
                variant.setImageUrl(vReq.getImageUrl());
                variant.setCreatedAt(LocalDateTime.now());
                variant.setUpdatedAt(LocalDateTime.now());
                variantRepository.save(variant);
            }

            // Calculate Min/Max Price and Total Stock for Product Display
            List<CreateProductVariantRequest> variantList = request.getVariants();
            BigDecimal minPrice = variantList.stream()
                    .map(CreateProductVariantRequest::getPrice)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            BigDecimal maxPrice = variantList.stream()
                    .map(CreateProductVariantRequest::getPrice)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            Integer totalStock = variantList.stream()
                    .mapToInt(CreateProductVariantRequest::getStock)
                    .sum();

            savedProduct.setMinPrice(minPrice);
            savedProduct.setMaxPrice(maxPrice);
            savedProduct.setTotalStock(totalStock);
            productRepository.save(savedProduct);
        }

        // 2. Save Images
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            int order = 0;
            for (String url : request.getImages()) {
                ProductImage image = new ProductImage();
                image.setProductId(savedProduct.getId());
                image.setImageUrl(url);
                image.setDisplayOrder(order++);
                image.setCreatedAt(LocalDateTime.now());
                imageRepository.save(image);
            }
        }

        // 3. Save Category (auto-detect from name if not provided)
        String categoryIdToUse = request.getCategoryId();
        if ((categoryIdToUse == null || categoryIdToUse.isEmpty()) && request.getName() != null) {
            String detectedName = CategoryDetectionUtil.detectFromName(request.getName());
            categoryIdToUse = categoryRepository.findByName(detectedName)
                    .map(c -> c.getId()).orElse(null);
        }
        if (categoryIdToUse != null && !categoryIdToUse.isEmpty()) {
            ProductCategory productCategory = new ProductCategory();
            productCategory.setProductId(savedProduct.getId());
            productCategory.setCategoryId(categoryIdToUse);
            productCategoryRepository.save(productCategory);
        }

        return mapToResponse(savedProduct);
    }

    @Override
    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToResponse(product);
    }

    @Override
    public List<ProductResponse> getProductsByShopId(String shopId) {
        return getProductsByShopId(shopId, false);
    }

    @Override
    public List<ProductResponse> getProductsByShopId(String shopId, boolean includeHidden) {
        List<Product> products;
        if (includeHidden) {
            products = productRepository.findByShopId(shopId);
        } else {
            products = productRepository.findByShopIdAndStatus(shopId, ProductStatus.ACTIVE);
        }
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getAllProducts(String sort) {
        org.springframework.data.domain.Sort sorting = org.springframework.data.domain.Sort.unsorted();

        if ("sold_desc".equals(sort)) {
            sorting = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                    "sold");
        }

        return productRepository.findByStatus(ProductStatus.ACTIVE, sorting).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // If no keyword is provided, return an empty list instead of all products
            return java.util.List.of();
        }

        String trimmed = keyword.trim();

        return productRepository
                .searchByNameOrDescriptionAndStatus(trimmed, ProductStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByCategory(String categoryId) {
        // 1. Get all product IDs linked to this category
        List<String> productIds = productCategoryRepository.findByCategoryId(categoryId).stream()
                .map(ProductCategory::getProductId)
                .collect(Collectors.toList());

        // 2. Fetch products details
        return productRepository.findAllById(productIds).stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(String id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Update basic fields
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        if (request.getIsFlashSale() != null) {
            product.setIsFlashSale(request.getIsFlashSale());
        }
        if (request.getFlashSalePrice() != null) {
            product.setFlashSalePrice(request.getFlashSalePrice());
        }
        if (request.getFlashSaleStock() != null) {
            product.setFlashSaleStock(request.getFlashSaleStock());
        }
        if (request.getShopId() != null) {
            product.setShopId(request.getShopId());
        }

        // Update Category
        if (request.getCategoryId() != null) {
            // Remove old categories
            productCategoryRepository.deleteByProductId(id);

            // Add new category
            ProductCategory productCategory = new ProductCategory();
            productCategory.setProductId(id);
            productCategory.setCategoryId(request.getCategoryId());
            productCategoryRepository.save(productCategory);
        }

        // Update Images
        if (request.getImages() != null) {
            // Remove old images
            imageRepository.deleteByProductId(id);

            // Add new images
            int order = 0;
            for (String url : request.getImages()) {
                ProductImage image = new ProductImage();
                image.setProductId(id);
                image.setImageUrl(url);
                image.setDisplayOrder(order++);
                image.setCreatedAt(LocalDateTime.now());
                imageRepository.save(image);
            }
        }

        // Update Variants
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            // Remove old variants
            variantRepository.deleteByProductId(id);

            // Add new variants
            for (CreateProductVariantRequest vReq : request.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProductId(id);
                variant.setSize(vReq.getSize());
                variant.setColor(vReq.getColor());
                variant.setPrice(vReq.getPrice());
                variant.setStock(vReq.getStock());
                variant.setImageUrl(vReq.getImageUrl());
                variant.setCreatedAt(LocalDateTime.now());
                variant.setUpdatedAt(LocalDateTime.now());
                variantRepository.save(variant);
            }

            // Recalculate Min/Max Price and Total Stock
            BigDecimal minPrice = request.getVariants().stream()
                    .map(CreateProductVariantRequest::getPrice)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            BigDecimal maxPrice = request.getVariants().stream()
                    .map(CreateProductVariantRequest::getPrice)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            Integer totalStock = request.getVariants().stream()
                    .mapToInt(CreateProductVariantRequest::getStock)
                    .sum();

            product.setMinPrice(minPrice);
            product.setMaxPrice(maxPrice);
            product.setTotalStock(totalStock);
        }

        product.setUpdatedAt(LocalDateTime.now());
        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }

        // Delete related data
        variantRepository.deleteByProductId(id);
        imageRepository.deleteByProductId(id);
        productCategoryRepository.deleteByProductId(id);

        productRepository.deleteById(id);
    }

    @Override
    public void addVariant(String productId, CreateProductVariantRequest request) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found");
        }

        ProductVariant variant = new ProductVariant();
        variant.setProductId(productId);
        variant.setSize(request.getSize());
        variant.setColor(request.getColor());
        variant.setPrice(request.getPrice());
        variant.setStock(request.getStock());
        variant.setImageUrl(request.getImageUrl());
        variant.setCreatedAt(LocalDateTime.now());
        variant.setUpdatedAt(LocalDateTime.now());

        variantRepository.save(variant);
    }

    @Override
    public void removeVariant(String variantId) {
        if (!variantRepository.existsById(variantId)) {
            throw new RuntimeException("Variant not found");
        }
        variantRepository.deleteById(variantId);
    }

    @Override
    public ProductVariantResponse getVariantById(String variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));
        return mapVariantToResponse(variant);
    }

    @Override
    @Transactional
    public void updateVariantStock(String variantId, Integer stock) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        variant.setStock(stock);
        variant.setUpdatedAt(LocalDateTime.now());
        variantRepository.save(variant);

        // Update Total Stock in Product
        updateProductTotalStock(variant.getProductId());
    }

    private void updateProductTotalStock(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ProductVariant> variants = variantRepository.findByProductId(productId);
        Integer totalStock = variants.stream()
                .mapToInt(ProductVariant::getStock)
                .sum();

        product.setTotalStock(totalStock);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    @Override
    public void addCategory(String productId, String categoryId) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found");
        }

        ProductCategory productCategory = new ProductCategory();
        productCategory.setProductId(productId);
        productCategory.setCategoryId(categoryId);

        productCategoryRepository.save(productCategory);
    }

    @Override
    public void removeCategory(String productId, String categoryId) {
        productCategoryRepository.deleteByProductIdAndCategoryId(productId, categoryId);
    }

    @Override
    public void addImage(String productId, String imageUrl) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found");
        }

        ProductImage image = new ProductImage();
        image.setProductId(productId);
        image.setImageUrl(imageUrl);
        image.setCreatedAt(LocalDateTime.now());

        imageRepository.save(image);
    }

    @Override
    public void removeImage(String imageId) {
        if (!imageRepository.existsById(imageId)) {
            throw new RuntimeException("Image not found");
        }
        imageRepository.deleteById(imageId);
    }

    @Override
    public List<ProductResponse> getFlashSaleProducts() {
        // Chỉ hiển thị sản phẩm flash sale nếu đang có chiến dịch flash sale đang chạy
        if (flashSaleService.getCurrentFlashSale().isEmpty()) {
            return java.util.List.of();
        }

        return productRepository.findByIsFlashSaleTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setShopId(product.getShopId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setStatus(product.getStatus());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        response.setMinPrice(product.getMinPrice());
        response.setMaxPrice(product.getMaxPrice());
        response.setTotalStock(product.getTotalStock());
        response.setSold(product.getSold());
        response.setRating(product.getRating());
        response.setReviewCount(product.getReviewCount() != null ? product.getReviewCount() : 0);
        response.setIsFlashSale(product.getIsFlashSale());
        response.setFlashSalePrice(product.getFlashSalePrice());
        response.setFlashSaleStock(product.getFlashSaleStock());

        // Load variants
        List<ProductVariantResponse> variants = variantRepository.findByProductId(product.getId()).stream()
                .map(this::mapVariantToResponse)
                .collect(Collectors.toList());
        response.setVariants(variants);

        // Load images
        List<String> images = imageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId()).stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
        response.setImages(images);

        // Load categories
        List<String> categories = productCategoryRepository.findByProductId(product.getId()).stream()
                .map(ProductCategory::getCategoryId)
                .collect(Collectors.toList());
        response.setCategories(categories);

        return response;
    }

    private ProductVariantResponse mapVariantToResponse(ProductVariant variant) {
        ProductVariantResponse response = new ProductVariantResponse();
        response.setId(variant.getId());
        response.setProductId(variant.getProductId());
        response.setSize(variant.getSize());
        response.setColor(variant.getColor());
        response.setPrice(variant.getPrice());
        response.setStock(variant.getStock());
        response.setImageUrl(variant.getImageUrl());
        response.setCreatedAt(variant.getCreatedAt());
        response.setUpdatedAt(variant.getUpdatedAt());
        return response;
    }

    @Override
    @Transactional
    public void updateProductFlashSaleStatus(String id, boolean isFlashSale) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setIsFlashSale(isFlashSale);
        if (!isFlashSale) {
            product.setFlashSalePrice(null);
            product.setFlashSaleStock(null);
        }
        // If isFlashSale is true, we assume the user will update price/stock via other
        // means or it's already set.
        // But the requirement here is mainly for "Delete" which sets it to false.

        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void updateProductVisibility(String id, ProductStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setStatus(status);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }
}
