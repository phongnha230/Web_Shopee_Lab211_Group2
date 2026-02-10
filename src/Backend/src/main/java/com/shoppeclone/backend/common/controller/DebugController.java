package com.shoppeclone.backend.common.controller;

import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.entity.ProductCategory;
import com.shoppeclone.backend.product.repository.CategoryRepository;
import com.shoppeclone.backend.product.repository.ProductCategoryRepository;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.product.util.CategoryDetectionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping("/product-categories")
    public ResponseEntity<List<ProductCategory>> getAllProductCategories() {
        return ResponseEntity.ok(productCategoryRepository.findAll());
    }

    /**
     * Fix category for a product by auto-detecting from product name.
     * Use when a product (e.g. a book) was created without correct category and voucher doesn't apply.
     * Example: POST /api/debug/products/6986f1fe3d133823d9e13c57/fix-category
     */
    @PostMapping("/products/{productId}/fix-category")
    public ResponseEntity<Map<String, String>> fixProductCategory(@PathVariable String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        String detectedName = CategoryDetectionUtil.detectFromName(product.getName());
        var categoryOpt = categoryRepository.findByName(detectedName);
        if (categoryOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Category not found: " + detectedName));
        }
        productCategoryRepository.deleteByProductId(productId);
        ProductCategory pc = new ProductCategory();
        pc.setProductId(productId);
        pc.setCategoryId(categoryOpt.get().getId());
        productCategoryRepository.save(pc);
        return ResponseEntity.ok(Map.of(
                "message", "Category fixed",
                "productId", productId,
                "productName", product.getName(),
                "category", detectedName));
    }
}
