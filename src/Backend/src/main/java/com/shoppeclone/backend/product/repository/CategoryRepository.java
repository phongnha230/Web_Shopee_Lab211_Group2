package com.shoppeclone.backend.product.repository;

import com.shoppeclone.backend.product.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CategoryRepository extends MongoRepository<Category, String> {
    List<Category> findByParentId(String parentId);

    List<Category> findByParentIdIsNull(); // Get root categories

    boolean existsByName(String name);

    java.util.Optional<Category> findByName(String name);
}
