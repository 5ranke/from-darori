package com.daroring.fromdarori.domain.newsletter.controller;

import com.daroring.fromdarori.domain.newsletter.dto.NewsletterDetailResponse;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterListResponse;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterYearsResponse;
import com.daroring.fromdarori.domain.newsletter.dto.PublishedListQuery;
import com.daroring.fromdarori.domain.newsletter.service.NewsletterService;
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
@RequestMapping("/api/v1/newletters")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterService newsletterService;

    @GetMapping("/years")
    public ApiResponse<NewsletterYearsResponse> getYears() {
        return ApiResponse.success("발행 연도 조회에 성공했습니다.", newsletterService.getPublishedYears());
    }

    @GetMapping
    public ApiResponse<NewsletterListResponse> getList(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size
    ) {
        PublishedListQuery query = PublishedListQuery.parse(year, page, size);
        return ApiResponse.success("발행호 목록 조회에 성공했습니다.",
                newsletterService.getPublishedList(query.year(), query.page(), query.size()));
    }

    @GetMapping("/{newsletterId}")
    public ApiResponse<NewsletterDetailResponse> getDetail(@PathVariable String newsletterId) {
        if (!newsletterId.matches("[0-9a-fA-F]{8}-(?:[0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "newsletterId는 UUID 형식이어야 합니다.");
        }
        return ApiResponse.success("발행호 상세 조회에 성공했습니다.",
                newsletterService.getPublishedDetail(UUID.fromString(newsletterId)));
    }
}
