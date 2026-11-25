package com.daroring.fromdarori.domain.newsletter.controller;

import com.daroring.fromdarori.domain.newsletter.dto.NewsletterListResponse;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterYearsResponse;
import com.daroring.fromdarori.domain.newsletter.service.NewsletterService;
import com.daroring.fromdarori.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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
        if (year != null && !year.matches("[1-9][0-9]{3}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "year는 네 자리 양의 정수여야 합니다.");
        }
        Integer selectedYear = year == null ? null : Integer.valueOf(year);
        int pageNumber = parsePageParameter(page, 0, 0, Integer.MAX_VALUE, "page는 0 이상의 정수여야 합니다.");
        int pageSize = parsePageParameter(size, 6, 1, 30, "size는 1~30 사이의 정수여야 합니다.");
        return ApiResponse.success("발행호 목록 조회에 성공했습니다.",
                newsletterService.getPublishedList(selectedYear, pageNumber, pageSize));
    }

    private int parsePageParameter(String value, int defaultValue, int min, int max, String errorMessage) {
        if (value == null) {
            return defaultValue;
        }
        try {
            int number = Integer.parseInt(value);
            if (number >= min && number <= max) {
                return number;
            }
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage, exception);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
    }

    // 발행호 상세 조회
    // GET /api/v1/newletters/{newsletterId}
}
