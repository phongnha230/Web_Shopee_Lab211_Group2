package com.shoppeclone.backend.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestionResponse {

    /** The keyword that was used to generate suggestions */
    private String keyword;

    /** Ordered list of suggestions (products first, then categories, keywords, attributes) */
    private List<SearchSuggestionItem> suggestions;
}
