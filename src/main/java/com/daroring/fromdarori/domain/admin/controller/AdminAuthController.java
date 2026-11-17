package com.daroring.fromdarori.domain.admin.controller;

import com.daroring.fromdarori.domain.admin.dto.AdminLoginRequest;
import com.daroring.fromdarori.domain.admin.dto.AdminLoginResponse;
import com.daroring.fromdarori.domain.admin.service.AdminAuthService;
import com.daroring.fromdarori.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    // 운영자 로그인
    // POST /api/v1/admin/auth/login
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AdminLoginResponse>> login(@Valid @RequestBody AdminLoginRequest request) {
        var result = adminAuthService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.refreshCookie().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(ApiResponse.success("로그인에 성공했습니다.", result.response()));
    }

    // Access Token 재발급
    // POST /api/v1/admin/auth/refresh

    // 로그인한 운영자 조회
    // GET /api/v1/admin/auth/me

    // 운영자 로그아웃
    // POST /api/v1/admin/auth/logout
}
