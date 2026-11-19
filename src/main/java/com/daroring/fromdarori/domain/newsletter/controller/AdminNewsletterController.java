package com.daroring.fromdarori.domain.newsletter.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/newsletters")
public class AdminNewsletterController {

    // AI 초안·퀴즈 생성
    // POST /api/v1/admin/newsletters/generate

    // 뉴스레터 저장
    // POST /api/v1/admin/newsletters

    // 저장된 뉴스레터 목록 조회
    // GET /api/v1/admin/newsletters

    // 저장된 뉴스레터 상세 조회
    // GET /api/v1/admin/newsletters/{newsletterId}

    // 선택한 뉴스레터 예약
    // PATCH /api/v1/admin/newsletters/{newsletterId}/reservation
}
