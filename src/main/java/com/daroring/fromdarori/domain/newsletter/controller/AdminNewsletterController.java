package com.daroring.fromdarori.domain.newsletter.controller;

import com.daroring.fromdarori.domain.newsletter.dto.NewsletterGenerateRequest;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterGenerateResponse;
import com.daroring.fromdarori.domain.newsletter.mail.NewsletterEmailRenderer;
import com.daroring.fromdarori.domain.newsletter.mail.NewsletterMailDraft;
import com.daroring.fromdarori.domain.newsletter.service.NewsletterGenerationService;
import com.daroring.fromdarori.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/newsletters")
public class AdminNewsletterController {

    private final NewsletterGenerationService generationService;
    private final NewsletterEmailRenderer emailRenderer;

    @PostMapping("/generate")
    public ApiResponse<NewsletterGenerateResponse> generate(@RequestBody(required = false) NewsletterGenerateRequest request) {
        return ApiResponse.success("뉴스레터가 생성되었습니다.", generationService.generate(request));
    }

    @PostMapping("/preview")
    public ApiResponse<EmailPreview> preview(@RequestBody NewsletterMailDraft draft) {
        if (draft.title() == null || draft.title().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "뉴스레터 제목을 입력해 주세요.");
        }
        return ApiResponse.success("이메일 미리보기를 만들었습니다.", new EmailPreview(emailRenderer.renderPreview(draft)));
    }

    public record EmailPreview(String html) {
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
