package com.daroring.fromdarori.domain.category.controller;

import com.daroring.fromdarori.domain.category.repository.CategoryRepository;
import com.daroring.fromdarori.global.response.ApiResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getCategories() {
        var categories = categoryRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(category -> new CategoryResponse(
                        category.getId(), category.getCode(), category.getName(), category.getDisplayOrder()))
                .toList();
        return ApiResponse.success("소식 카테고리 조회에 성공했습니다.", categories);
    }

    public record CategoryResponse(UUID categoryId, String code, String name, Integer displayOrder) {}
}
