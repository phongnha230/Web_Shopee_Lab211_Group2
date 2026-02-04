package com.shoppeclone.backend.common.controller;

import com.shoppeclone.backend.product.entity.ProductCategory;
import com.shoppeclone.backend.product.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final ProductCategoryRepository productCategoryRepository;

    @GetMapping("/product-categories")
    public ResponseEntity<List<ProductCategory>> getAllProductCategories() {
        return ResponseEntity.ok(productCategoryRepository.findAll());
    }
}
