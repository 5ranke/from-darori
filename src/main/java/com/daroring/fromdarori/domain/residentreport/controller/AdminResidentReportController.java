package com.daroring.fromdarori.domain.residentreport.controller;

import com.daroring.fromdarori.domain.residentreport.dto.ReportIdInfoResponse;
import com.daroring.fromdarori.domain.residentreport.dto.ReportInfoResponse;
import com.daroring.fromdarori.domain.residentreport.dto.ReportToStoryResponse;
import com.daroring.fromdarori.domain.residentreport.service.AdminResidentReportService;
import com.daroring.fromdarori.domain.story.dto.StoryCreateRequest;
import com.daroring.fromdarori.domain.story.exception.ApiException;
import com.daroring.fromdarori.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/resident-reports")
public class AdminResidentReportController {

    private final AdminResidentReportService adminResidentReportService;

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

    private static UUID parseReportId(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "REPORT4001", "reportId는 올바른 UUID 형식이어야 합니다.");
        }
    }

    // 받은 제보 목록 조회
    // GET /api/v1/admin/resident-reports
    @GetMapping
    public ResponseEntity<ApiResponse<ReportInfoResponse>> getReports(
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size
    ) {
        int pageValue = parseIntOrDefault(page, 0, "page");
        int sizeValue = parseIntOrDefault(size, 10, "size");
        if (pageValue < 0 || sizeValue < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "REPORT_QUERY4001", "page는 0 이상, size는 1 이상이어야 합니다.");
        }

        ReportInfoResponse response = adminResidentReportService.getReports(pageValue,sizeValue);
        return ResponseEntity.ok(ApiResponse.success("받은 제보 목록 조회에 성공했습니다.", response));
    }

    // 제보 상세 조회
    // GET /api/v1/admin/resident-reports/{reportId}
    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<ReportIdInfoResponse>> getReport(
            @PathVariable String reportId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ReportIdInfoResponse response = adminResidentReportService.getReport(parseReportId(reportId));
        return ResponseEntity.ok(ApiResponse.success("제보 상세 조회에 성공했습니다.", response));
    }

    // 제보를 소식으로 등록
    // POST /api/v1/admin/resident-reports/{reportId}/story
    @PostMapping("/{reportId}/story")
    public ResponseEntity<ApiResponse<ReportToStoryResponse>> createStory(
            @PathVariable String reportId,
            @RequestBody @Valid StoryCreateRequest request,
            @AuthenticationPrincipal Jwt jwt
            ) {
        ReportToStoryResponse response = adminResidentReportService.createStory(request, UUID.fromString(jwt.getSubject()), parseReportId(reportId));
        return ResponseEntity.status(201)
                .body(ApiResponse.created("제보가 소식으로 등록되었습니다.", response));
    }

    // 제보 삭제
    // DELETE /api/v1/admin/resident-reports/{reportId}
}
