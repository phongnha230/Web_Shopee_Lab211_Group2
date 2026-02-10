package com.shoppeclone.backend.product.controller;

import com.shoppeclone.backend.product.dto.request.CreateProductRequest;
import com.shoppeclone.backend.product.dto.request.CreateProductVariantRequest;
import com.shoppeclone.backend.product.dto.request.UpdateProductRequest;
import com.shoppeclone.backend.product.dto.response.ProductResponse;
import com.shoppeclone.backend.product.dto.response.ProductVariantResponse;
import com.shoppeclone.backend.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<ProductResponse>> getProductsByShopId(@PathVariable String shopId) {
        return ResponseEntity.ok(productService.getProductsByShopId(shopId));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(@RequestParam(required = false) String sort) {
        return ResponseEntity.ok(productService.getAllProducts(sort));
    }

    @GetMapping("/flash-sale")
    public ResponseEntity<List<ProductResponse>> getFlashSaleProducts() {
        return ResponseEntity.ok(productService.getFlashSaleProducts());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable String categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateProductStatus(
            @PathVariable String id,
            @RequestBody Map<String, Object> updates) {

        if (updates.containsKey("isFlashSale")) {
            boolean isFlashSale = (Boolean) updates.get("isFlashSale");
            productService.updateProductFlashSaleStatus(id, isFlashSale);
        }

        return ResponseEntity.ok().build();
    }

    // Variant management
    @PostMapping("/{productId}/variants")
    public ResponseEntity<Map<String, String>> addVariant(
            @PathVariable String productId,
            @Valid @RequestBody CreateProductVariantRequest request) {
        request.setProductId(productId);
        productService.addVariant(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Variant added successfully"));
    }

    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<Void> removeVariant(@PathVariable String variantId) {
        productService.removeVariant(variantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/variant/{variantId}")
    public ResponseEntity<ProductVariantResponse> getVariantById(@PathVariable String variantId) {
        return ResponseEntity.ok(productService.getVariantById(variantId));
    }

    @PatchMapping("/variant/{variantId}/stock")
    public ResponseEntity<Void> updateVariantStock(
            @PathVariable String variantId,
            @RequestParam Integer stock) {
        productService.updateVariantStock(variantId, stock);
        return ResponseEntity.ok().build();
    }

    // Category management
    @PostMapping("/{productId}/categories/{categoryId}")
    public ResponseEntity<Map<String, String>> addCategory(
            @PathVariable String productId,
            @PathVariable String categoryId) {
        productService.addCategory(productId, categoryId);
        return ResponseEntity.ok(Map.of("message", "Category added to product"));
    }

    @DeleteMapping("/{productId}/categories/{categoryId}")
    public ResponseEntity<Void> removeCategory(
            @PathVariable String productId,
            @PathVariable String categoryId) {
        productService.removeCategory(productId, categoryId);
        return ResponseEntity.noContent().build();
    }

    // Image management
    @PostMapping("/{productId}/images")
    public ResponseEntity<Map<String, String>> addImage(
            @PathVariable String productId,
            @RequestBody Map<String, String> request) {
        productService.addImage(productId, request.get("imageUrl"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Image added successfully"));
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> removeImage(@PathVariable String imageId) {
        productService.removeImage(imageId);
        return ResponseEntity.noContent().build();
    }
}
