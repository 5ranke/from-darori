package com.daroring.fromdarori.domain.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {

    // 운영자 로그인
    // POST /api/v1/admin/auth/login

    // Access Token 재발급
    // POST /api/v1/admin/auth/refresh

    // 로그인한 운영자 조회
    // GET /api/v1/admin/auth/me

    // 운영자 로그아웃
    // POST /api/v1/admin/auth/logout
}
