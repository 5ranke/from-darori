package com.daroring.fromdarori.domain.newsletter.controller;

import com.daroring.fromdarori.domain.newsletter.dto.AdminNewsletterDetailResponse;
import com.daroring.fromdarori.domain.newsletter.dto.AdminNewsletterListResponse;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterGenerateRequest;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterGenerateResponse;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterReservationResponse;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterSaveRequest;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterSaveResponse;
import com.daroring.fromdarori.domain.newsletter.mail.NewsletterEmailRenderer;
import com.daroring.fromdarori.domain.newsletter.mail.NewsletterMailDraft;
import com.daroring.fromdarori.domain.newsletter.service.AdminNewsletterDetailService;
import com.daroring.fromdarori.domain.newsletter.service.AdminNewsletterListService;
import com.daroring.fromdarori.domain.newsletter.service.NewsletterGenerationService;
import com.daroring.fromdarori.domain.newsletter.service.NewsletterReservationService;
import com.daroring.fromdarori.domain.newsletter.service.NewsletterSaveService;
import com.daroring.fromdarori.global.response.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/newsletters")
public class AdminNewsletterController {

    private final NewsletterGenerationService generationService;
    private final NewsletterEmailRenderer emailRenderer;
    private final NewsletterSaveService saveService;
    private final AdminNewsletterListService listService;
    private final AdminNewsletterDetailService detailService;
    private final NewsletterReservationService reservationService;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NewsletterSaveResponse> save(
            @RequestBody(required = false) NewsletterSaveRequest request, @AuthenticationPrincipal Jwt principal
    ) {
        return ApiResponse.created("뉴스레터가 저장되었습니다.", saveService.save(request, UUID.fromString(principal.getSubject())));
    }

    @GetMapping
    public ApiResponse<AdminNewsletterListResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size
    ) {
        return ApiResponse.success("뉴스레터 목록을 조회했습니다.", listService.list(status, page, size));
    }

    @GetMapping("/{newsletterId}")
    public ApiResponse<AdminNewsletterDetailResponse> getDetail(@PathVariable UUID newsletterId) {
        return ApiResponse.success("뉴스레터 상세 정보를 조회했습니다.", detailService.getDetail(newsletterId));
    }

    @DeleteMapping("/{newsletterId}")
    public ApiResponse<NewsletterWithdrawal> withdraw(@PathVariable UUID newsletterId) {
        return ApiResponse.success("뉴스레터 저장이 취소되었습니다.", new NewsletterWithdrawal(saveService.withdraw(newsletterId)));
    }

    public record NewsletterWithdrawal(UUID newsletterId) {
    }

    @PatchMapping("/{newsletterId}/reservation")
    public ApiResponse<NewsletterReservationResponse> reserve(@PathVariable UUID newsletterId) {
        return ApiResponse.success("뉴스레터 발행이 예약되었습니다.", reservationService.reserve(newsletterId));
    }

    @DeleteMapping("/{newsletterId}/reservation")
    public ApiResponse<NewsletterReservationResponse> cancelReservation(@PathVariable UUID newsletterId) {
        return ApiResponse.success("뉴스레터 발행 예약이 취소되었습니다.", reservationService.cancel(newsletterId));
    }
}
