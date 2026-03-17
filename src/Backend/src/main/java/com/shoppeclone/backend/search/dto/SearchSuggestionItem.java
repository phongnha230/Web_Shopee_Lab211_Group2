package com.shoppeclone.backend.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestionItem {

    public enum SuggestionType {
        PRODUCT,    // A specific matching product
        CATEGORY,   // A matching category
        KEYWORD,    // A suggested search keyword derived from product names
        ATTRIBUTE   // A matching variant attribute (color, size)
    }

    private SuggestionType type;

    /** Human-readable label shown in the dropdown */
    private String label;

    /** Entity ID — present for PRODUCT and CATEGORY types */
    private String id;

    /** Thumbnail URL — present for PRODUCT type */
    private String imageUrl;

    /** Lowest price — present for PRODUCT type */
    private BigDecimal minPrice;

    /** Total units sold — present for PRODUCT type */
    private Integer sold;

    /** Attribute kind ("color" or "size") — present for ATTRIBUTE type */
    private String attributeType;
}
