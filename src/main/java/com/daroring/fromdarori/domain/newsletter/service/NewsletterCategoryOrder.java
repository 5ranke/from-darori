package com.daroring.fromdarori.domain.newsletter.service;

import com.daroring.fromdarori.domain.category.entity.Category;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public final class NewsletterCategoryOrder {

    private NewsletterCategoryOrder() {
    }

    public static <T> Comparator<T> byCategory(
            List<Category> categories, Function<T, UUID> categoryId, Function<T, String> categoryName
    ) {
        Map<UUID, Integer> byId = new HashMap<>();
        Map<String, Integer> byName = new HashMap<>();
        for (Category category : categories) {
            byId.put(category.getId(), category.getDisplayOrder());
            byName.put(category.getName(), category.getDisplayOrder());
        }
        return Comparator.comparingInt(item -> {
            Integer order = byId.get(categoryId.apply(item));
            return order == null ? byName.getOrDefault(categoryName.apply(item), Integer.MAX_VALUE) : order;
        });
    }
}
