package com.shoppeclone.backend.product.service;

import com.shoppeclone.backend.product.dto.request.CreateCategoryRequest;
import com.shoppeclone.backend.product.entity.Category;
import java.util.List;

public interface CategoryService {
    Category createCategory(CreateCategoryRequest request);

    Category getCategoryById(String id);

    List<Category> getAllCategories();

    List<Category> getRootCategories();

    List<Category> getSubCategories(String parentId);

    Category updateCategory(String id, CreateCategoryRequest request);

    void deleteCategory(String id);
}
