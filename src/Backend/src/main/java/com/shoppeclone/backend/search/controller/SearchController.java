package com.shoppeclone.backend.search.controller;

import com.shoppeclone.backend.search.dto.SearchSuggestionResponse;
import com.shoppeclone.backend.search.service.SearchSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SearchController {

    private final SearchSuggestionService searchSuggestionService;

    /**
     * GET /api/search/suggestions?keyword=áo&limit=10
     *
     * Returns a mixed list of suggestions for the homepage search box:
     * - PRODUCT  : matching product names with thumbnail + price
     * - CATEGORY : matching category names
     * - KEYWORD  : derived search phrases from product names
     * - ATTRIBUTE: matching variant colors / sizes
     *
     * @param keyword user input (required)
     * @param limit   max suggestions to return (default 10, max 20)
     */
    @GetMapping("/suggestions")
    public ResponseEntity<SearchSuggestionResponse> getSuggestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {

        int safeLimit = Math.min(Math.max(limit, 1), 20);
        return ResponseEntity.ok(searchSuggestionService.getSuggestions(keyword, safeLimit));
    }
}
