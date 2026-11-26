package com.daroring.fromdarori.domain.subscriber.controller;

import com.daroring.fromdarori.domain.subscriber.dto.SubscribeCreateRequest;
import com.daroring.fromdarori.domain.subscriber.dto.SubscribeCreateResponse;
import com.daroring.fromdarori.domain.subscriber.dto.UnsubscribeRequest;
import com.daroring.fromdarori.domain.subscriber.dto.UnsubscribeResponse;
import com.daroring.fromdarori.domain.subscriber.service.SubscriptionService;
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
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    // 구독 신청·재구독
    // POST /api/v1/subscriptions
    @PostMapping
    public ResponseEntity<ApiResponse<SubscribeCreateResponse>> subscribe(
            @RequestBody @Valid SubscribeCreateRequest request
            ) {
        SubscribeCreateResponse response = subscriptionService.subscribe(request);
        return ResponseEntity.status(200)
                .body(ApiResponse.success("뉴스레터 구독 신청에 성공했습니다.", response));
    }

    // 구독 해지 요청
    // POST /api/v1/subscriptions/unsubscribe
    @PostMapping("/unsubscribe")
    public ResponseEntity<ApiResponse<UnsubscribeResponse>> unsubscribe(
            @RequestBody @Valid UnsubscribeRequest request
            ) {
        UnsubscribeResponse response = subscriptionService.unsubscribe(request);
        return ResponseEntity.status(200)
                .body(ApiResponse.success("뉴스레터 구독 해지에 성공했습니다.", response));
    }
}
