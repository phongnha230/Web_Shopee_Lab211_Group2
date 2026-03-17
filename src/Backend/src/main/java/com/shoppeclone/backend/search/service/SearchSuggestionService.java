package com.shoppeclone.backend.search.service;

import com.shoppeclone.backend.search.dto.SearchSuggestionResponse;

public interface SearchSuggestionService {

    /**
     * Returns a mixed list of suggestions for the given keyword:
     * products, categories, derived keywords, and variant attributes.
     *
     * @param keyword raw user input from the search box
     * @param limit   max total suggestions to return (default 10)
     */
    SearchSuggestionResponse getSuggestions(String keyword, int limit);
}
