package com.daroring.fromdarori.domain.subscriber.controller;

import com.daroring.fromdarori.domain.subscriber.dto.AdminSubscriberCreateRequest;
import com.daroring.fromdarori.domain.subscriber.dto.AdminSubscriberCreateResponse;
import com.daroring.fromdarori.domain.subscriber.service.AdminSubscriberService;
import com.daroring.fromdarori.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/subscribers")
public class AdminSubscriberController {
    private final AdminSubscriberService adminSubscriberService;

    // 현재 구독자 목록 조회
    // GET /api/v1/admin/subscribers

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
