package com.daroring.fromdarori.domain.newsletter.controller;

import com.daroring.fromdarori.domain.newsletter.dto.NewsletterGenerateRequest;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterGenerateResponse;
import com.daroring.fromdarori.domain.newsletter.service.NewsletterGenerationService;
import com.daroring.fromdarori.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/newsletters")
public class AdminNewsletterController {

    private final NewsletterGenerationService generationService;

    @PostMapping("/generate")
    public ApiResponse<NewsletterGenerateResponse> generate(@RequestBody(required = false) NewsletterGenerateRequest request) {
        return ApiResponse.success("뉴스레터가 생성되었습니다.", generationService.generate(request));
    }

    // 뉴스레터 저장
    // POST /api/v1/admin/newsletters

    // 저장된 뉴스레터 목록 조회
    // GET /api/v1/admin/newsletters

    // 저장된 뉴스레터 상세 조회
    // GET /api/v1/admin/newsletters/{newsletterId}

    // 선택한 뉴스레터 예약
    // PATCH /api/v1/admin/newsletters/{newsletterId}/reservation
}
