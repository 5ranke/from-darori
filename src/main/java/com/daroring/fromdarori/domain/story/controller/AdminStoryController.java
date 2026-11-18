package com.daroring.fromdarori.domain.story.controller;

import com.daroring.fromdarori.domain.story.dto.*;
import com.daroring.fromdarori.domain.story.exception.ApiException;
import com.daroring.fromdarori.domain.story.service.AdminStoryService;
import com.daroring.fromdarori.domain.story.entity.StoryPublicationStatus;
import com.daroring.fromdarori.domain.story.entity.StorySourceType;
import com.daroring.fromdarori.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/stories")
public class AdminStoryController {

    private final AdminStoryService adminStoryService;

    private static int parseIntOrDefault(String value, int defaultValue, String parameterName) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "STORY_QUERY4002",parameterName + "은(는) 정수여야 합니다.");
        }
    }

    private static UUID parseUuidOrNull(String value, String parameterName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "STORY_QUERY4003",parameterName + "은(는) 올바른 UUID여야 합니다.");
        }
    }

    private static <E extends Enum<E>> E parseEnumOrNull(String value, Class<E> enumType, String parameterName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "STORY_QUERY4004",parameterName + " 값이 올바르지 않습니다.");
        }
    }

    private static UUID parseStoryId(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "STORY4001", "storyId는 올바른 UUID 형식이어야 합니다.");
        }
    }

    // 저장된 소식 목록 조회
    // GET /api/v1/admin/stories
    @GetMapping
    public ResponseEntity<ApiResponse<StoryInfoResponse>> getStories(
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String publicationStatus
    ) {
        int pageValue = parseIntOrDefault(page, 0, "page");
        int sizeValue = parseIntOrDefault(size, 10, "size");
        if (pageValue < 0 || sizeValue < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "STORY_QUERY4001", "page는 0 이상, size는 1 이상이어야 합니다.");
        }

        StoryInfoResponse response = adminStoryService.getStories(
                pageValue,
                sizeValue,
                parseUuidOrNull(categoryId, "categoryId"),
                parseEnumOrNull(sourceType, StorySourceType.class, "sourceType"),
                parseEnumOrNull(publicationStatus, StoryPublicationStatus.class, "publicationStatus")
        );
        return ResponseEntity.ok(ApiResponse.success("소식 목록 조회에 성공했습니다.", response));
    }

    // 소식 상세 조회
    // GET /api/v1/admin/stories/{storyId}
    @GetMapping("/{storyId}")
    public ResponseEntity<ApiResponse<StoryIdInfoResponse>> getStory(@PathVariable String storyId) {
        StoryIdInfoResponse response = adminStoryService.getStory(parseStoryId(storyId));
        return ResponseEntity.ok(ApiResponse.success("소식 상세 조회에 성공했습니다.", response));
    }

    // 소식 등록
    // POST /api/v1/admin/stories
    @PostMapping
    public ResponseEntity<ApiResponse<StoryCreateResponse>> create(
            @RequestBody @Valid StoryCreateRequest request,
            @AuthenticationPrincipal Jwt jwt
            ) {
        StoryCreateResponse response = adminStoryService.create(request, UUID.fromString(jwt.getSubject()));
        return ResponseEntity.status(201)
                .body(ApiResponse.success("소식이 등록되었습니다.", response));
    }

    // 소식 수정
    // PATCH /api/v1/admin/stories/{storyId}
    @PatchMapping("/{storyId}")
    public ResponseEntity<ApiResponse<StoryUpdateResponse>> update(
            @PathVariable String storyId,
            @RequestBody @Valid StoryUpdateRequest request
    ) {
        StoryUpdateResponse response = adminStoryService.update(parseStoryId(storyId), request);
        return ResponseEntity.ok(ApiResponse.success("소식이 수정되었습니다.",response));
    }

    // 소식 삭제
    // DELETE /api/v1/admin/stories/{storyId}
}
