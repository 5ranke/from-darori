package com.daroring.fromdarori.domain.subscriber.controller;

import com.daroring.fromdarori.domain.story.exception.ApiException;
import com.daroring.fromdarori.domain.subscriber.dto.AdminSubscriberCreateRequest;
import com.daroring.fromdarori.domain.subscriber.dto.AdminSubscriberCreateResponse;
import com.daroring.fromdarori.domain.subscriber.dto.SubscriberInfoResponse;
import com.daroring.fromdarori.domain.subscriber.service.AdminSubscriberService;
import com.daroring.fromdarori.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/subscribers")
public class AdminSubscriberController {
    private final AdminSubscriberService adminSubscriberService;

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

    // 현재 구독자 목록 조회
    // GET /api/v1/admin/subscribers
    @GetMapping
    public ResponseEntity<ApiResponse<SubscriberInfoResponse>> getSubscribers(
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size
    ) {
        int pageValue = parseIntOrDefault(page, 0, "page");
        int sizeValue = parseIntOrDefault(size, 10, "size");
        if(pageValue < 0 || sizeValue < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SUBSCRIBER_QUERY4001", "page는 0 이상, size는 1 이상이어야 합니다.");
        }

        SubscriberInfoResponse response = adminSubscriberService.getSubscribers(pageValue, sizeValue);
        return ResponseEntity.ok(ApiResponse.success("현재 구독자 목록 조회에 성공했습니다.", response));
    }

    // 구독자 추가
    // POST /api/v1/admin/subscribers
    @PostMapping
    public ResponseEntity<ApiResponse<AdminSubscriberCreateResponse>> create(
            @RequestBody @Valid AdminSubscriberCreateRequest request
            ) {
        AdminSubscriberCreateResponse response = adminSubscriberService.create(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("구독자 추가에 성공했습니다.",response));
    }

    // 구독자 삭제
    // DELETE /api/v1/admin/subscribers/{subscriberId}
}
