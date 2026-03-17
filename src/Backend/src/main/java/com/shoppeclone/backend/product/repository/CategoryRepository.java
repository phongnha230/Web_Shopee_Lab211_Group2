package com.shoppeclone.backend.product.repository;

import com.shoppeclone.backend.product.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface CategoryRepository extends MongoRepository<Category, String> {
    List<Category> findByParentId(String parentId);

    List<Category> findByParentIdIsNull(); // Get root categories

    boolean existsByName(String name);

    java.util.Optional<Category> findByName(String name);

    // Case-insensitive category name search for suggestions
    @Query("{ 'name': { '$regex': ?0, '$options': 'i' } }")
    List<Category> findByNameRegex(String nameRegex);
}
