package com.daroring.fromdarori.domain.residentreport.controller;

import com.daroring.fromdarori.domain.residentreport.dto.ResidentReportRequest;
import com.daroring.fromdarori.domain.residentreport.dto.ResidentReportResponse;
import com.daroring.fromdarori.domain.residentreport.service.ResidentReportService;
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
@RequestMapping("/api/v1/resident-reports")
public class ResidentReportController {
    private final ResidentReportService residentReportService;

    // 제보 접수
    // POST /api/v1/resident-reports
    @PostMapping
    public ResponseEntity<ApiResponse<ResidentReportResponse>> create(
            @RequestBody @Valid ResidentReportRequest request
            ) {
        ResidentReportResponse response = residentReportService.create(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("제보가 등록되었습니다.", response));
    }
}
