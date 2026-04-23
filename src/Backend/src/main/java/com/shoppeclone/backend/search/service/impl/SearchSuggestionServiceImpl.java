package com.shoppeclone.backend.search.service.impl;

import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.entity.ProductImage;
import com.shoppeclone.backend.product.entity.ProductStatus;
import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.product.repository.CategoryRepository;
import com.shoppeclone.backend.product.repository.ProductImageRepository;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import com.shoppeclone.backend.search.dto.SearchSuggestionItem;
import com.shoppeclone.backend.search.dto.SearchSuggestionItem.SuggestionType;
import com.shoppeclone.backend.search.dto.SearchSuggestionResponse;
import com.shoppeclone.backend.search.service.SearchSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchSuggestionServiceImpl implements SearchSuggestionService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public SearchSuggestionResponse getSuggestions(String keyword, int limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return SearchSuggestionResponse.builder()
                    .keyword("")
                    .suggestions(List.of())
                    .build();
        }

        String trimmed = keyword.trim();
        String safeRegex = escapeRegex(trimmed);

        List<SearchSuggestionItem> suggestions = new ArrayList<>();

        // ── 1. Product suggestions ──────────────────────────────────────────────
        // Top matches by name, sorted by sold descending
        int productLimit = Math.max(1, limit / 2);
        List<Product> topProducts = productRepository
                .findByNameRegexAndStatus(
                        safeRegex,
                        ProductStatus.ACTIVE,
                        PageRequest.of(0, productLimit, Sort.by(Sort.Direction.DESC, "sold")))
                .getContent();

        for (Product product : topProducts) {
            // Use the first image as thumbnail
            String imageUrl = productImageRepository
                    .findByProductIdOrderByDisplayOrderAsc(product.getId())
                    .stream()
                    .findFirst()
                    .map(ProductImage::getImageUrl)
                    .orElse(null);

            suggestions.add(SearchSuggestionItem.builder()
                    .type(SuggestionType.PRODUCT)
                    .label(product.getName())
                    .id(product.getId())
                    .imageUrl(imageUrl)
                    .minPrice(product.getMinPrice())
                    .sold(product.getSold())
                    .build());
        }

        // ── 2. Category suggestions ─────────────────────────────────────────────
        int categoryLimit = Math.max(1, limit / 5);
        categoryRepository.findByNameRegex(safeRegex).stream()
                .limit(categoryLimit)
                .forEach(cat -> suggestions.add(SearchSuggestionItem.builder()
                        .type(SuggestionType.CATEGORY)
                        .label(cat.getName())
                        .id(cat.getId())
                        .build()));

        // ── 3. Keyword suggestions ──────────────────────────────────────────────
        // Distinct phrases derived from a broader set of matching product names
        int keywordLimit = Math.max(1, limit / 4);

        Set<String> productNamesShown = topProducts.stream()
                .map(p -> p.getName().toLowerCase())
                .collect(Collectors.toSet());

        List<Product> broadProducts = productRepository
                .findByNameRegexAndStatus(
                        safeRegex,
                        ProductStatus.ACTIVE,
                        PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "sold")))
                .getContent();

        Set<String> keywordSet = new LinkedHashSet<>();
        for (Product product : broadProducts) {
            String nameLower = product.getName().toLowerCase();

            // Full name (if not already a PRODUCT suggestion)
            if (!productNamesShown.contains(nameLower)) {
                keywordSet.add(product.getName());
            }

            // Sub-phrases starting with the keyword token
            String[] tokens = product.getName().split("\\s+");
            for (int i = 0; i < tokens.length; i++) {
                if (tokens[i].toLowerCase().startsWith(trimmed.toLowerCase())) {
                    StringBuilder phrase = new StringBuilder(tokens[i]);
                    for (int j = i + 1; j < Math.min(i + 3, tokens.length); j++) {
                        phrase.append(" ").append(tokens[j]);
                    }
                    keywordSet.add(phrase.toString());
                }
            }
            if (keywordSet.size() >= keywordLimit * 4) break;
        }

        keywordSet.stream()
                .limit(keywordLimit)
                .forEach(kw -> suggestions.add(SearchSuggestionItem.builder()
                        .type(SuggestionType.KEYWORD)
                        .label(kw)
                        .build()));

        // ── 4. Attribute suggestions (color & size) ─────────────────────────────
        int attributeLimit = Math.max(1, limit / 5);

        Set<String> seenColors = productVariantRepository.findByColorRegex(safeRegex).stream()
                .map(ProductVariant::getColor)
                .filter(c -> c != null && !c.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        seenColors.stream()
                .limit(attributeLimit)
                .forEach(color -> suggestions.add(SearchSuggestionItem.builder()
                        .type(SuggestionType.ATTRIBUTE)
                        .label(color)
                        .attributeType("color")
                        .build()));

        Set<String> seenSizes = productVariantRepository.findBySizeRegex(safeRegex).stream()
                .map(ProductVariant::getSize)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        seenSizes.stream()
                .limit(attributeLimit)
                .forEach(size -> suggestions.add(SearchSuggestionItem.builder()
                        .type(SuggestionType.ATTRIBUTE)
                        .label(size)
                        .attributeType("size")
                        .build()));

        // Cap total results to requested limit
        List<SearchSuggestionItem> result = suggestions.size() > limit
                ? suggestions.subList(0, limit)
                : suggestions;

        return SearchSuggestionResponse.builder()
                .keyword(trimmed)
                .suggestions(result)
                .build();
    }

    /**
     * Escapes MongoDB/Java regex metacharacters so user input is treated as a
     * literal prefix/substring, preventing regex injection.
     */
    private String escapeRegex(String input) {
        return input.replaceAll("([\\\\^$.|?*+()\\[\\]{])", "\\\\$1");
    }
}
