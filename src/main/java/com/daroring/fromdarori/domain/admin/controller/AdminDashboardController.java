package com.daroring.fromdarori.domain.admin.controller;

import com.daroring.fromdarori.domain.admin.dto.AdminDashboardResponse;
import com.daroring.fromdarori.domain.admin.service.AdminDashboardService;
import com.daroring.fromdarori.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    // 대시보드 조회
    // GET /api/v1/admin/dashboard
    @GetMapping
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(ApiResponse.success("대시보드 정보를 조회했습니다.", adminDashboardService.getDashboard()));
    }
}
