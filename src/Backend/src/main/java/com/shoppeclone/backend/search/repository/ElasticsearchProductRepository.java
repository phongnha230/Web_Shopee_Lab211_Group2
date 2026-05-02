package com.shoppeclone.backend.search.repository;

import com.shoppeclone.backend.search.entity.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElasticsearchProductRepository extends ElasticsearchRepository<ProductDocument, String> {

    // Custom query to search by name or description allowing typos (fuzzy search)
    @Query("{\"bool\": {\"must\": [{\"match\": {\"status\": \"?1\"}}], \"should\": [{\"match\": {\"name\": {\"query\": \"?0\", \"fuzziness\": \"AUTO\"}}}, {\"match\": {\"description\": {\"query\": \"?0\", \"fuzziness\": \"AUTO\"}}}], \"minimum_should_match\": 1}}")
    List<ProductDocument> searchByNameOrDescriptionFuzzyAndStatus(String keyword, String status);

}
