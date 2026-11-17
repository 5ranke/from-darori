package com.daroring.fromdarori.domain.admin.controller;

import com.daroring.fromdarori.domain.admin.dto.AdminUserListResponse;
import com.daroring.fromdarori.domain.admin.dto.AdminUserCreateRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.daroring.fromdarori.domain.admin.service.AdminUserService;
import com.daroring.fromdarori.global.response.ApiResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    // 운영자 계정 목록 조회
    // GET /api/v1/admin/users
    @GetMapping
    public ResponseEntity<ApiResponse<AdminUserListResponse>> list(
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "4") @Min(1) int size) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(ApiResponse.success("운영자 목록을 조회했습니다.", adminUserService.list(page, size)));
    }

    // 운영자 계정 추가
    // POST /api/v1/admin/users
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AdminUserListResponse.UserInfo>> create(
            @Valid @RequestBody AdminUserCreateRequest request) {
        var user = adminUserService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(new ApiResponse<>(true, "COMMON201", "운영자 계정이 추가되었습니다.", user));
    }

    // 운영자 계정 삭제
    // DELETE /api/v1/admin/users/{adminUserId}
}
