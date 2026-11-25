package com.daroring.fromdarori.domain.story.controller;

import com.daroring.fromdarori.domain.newsletter.dto.PublishedListQuery;
import com.daroring.fromdarori.domain.story.dto.PublishedStoryDetailResponse;
import com.daroring.fromdarori.domain.story.dto.PublishedStoryListResponse;
import com.daroring.fromdarori.domain.story.service.PublishedStoryService;
import com.daroring.fromdarori.global.response.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/published-stories")
@RequiredArgsConstructor
public class PublishedStoryController {

    private static final String UUID_FORMAT = "[0-9a-fA-F]{8}-(?:[0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}";

    private final PublishedStoryService publishedStoryService;

    @GetMapping
    public ApiResponse<PublishedStoryListResponse> getList(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size
    ) {
        if (categoryId == null || !categoryId.matches(UUID_FORMAT)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryId는 UUID 형식이어야 합니다.");
        }
        PublishedListQuery query = PublishedListQuery.parse(year, page, size);
        return ApiResponse.success("카테고리별 수록 소식 조회에 성공했습니다.",
                publishedStoryService.getPublishedList(UUID.fromString(categoryId), query));
    }

    @GetMapping("/{publishedStoryId}")
    public ApiResponse<PublishedStoryDetailResponse> getDetail(@PathVariable String publishedStoryId) {
        if (!publishedStoryId.matches(UUID_FORMAT)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "publishedStoryId는 UUID 형식이어야 합니다.");
        }
        return ApiResponse.success("수록 소식 상세 조회에 성공했습니다.",
                publishedStoryService.getPublishedDetail(UUID.fromString(publishedStoryId)));
    }
}
