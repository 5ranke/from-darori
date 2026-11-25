package com.daroring.fromdarori.domain.newsletter.controller;

import com.daroring.fromdarori.domain.newsletter.dto.NewsletterYearsResponse;
import com.daroring.fromdarori.domain.newsletter.service.NewsletterService;
import com.daroring.fromdarori.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/newletters")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterService newsletterService;

    @GetMapping("/years")
    public ApiResponse<NewsletterYearsResponse> getYears() {
        return ApiResponse.success("발행 연도 조회에 성공했습니다.", newsletterService.getPublishedYears());
    }

    // 발행호 목록 조회
    // GET /api/v1/newletters

    // 발행호 상세 조회
    // GET /api/v1/newletters/{newsletterId}
}
