package com.daroring.fromdarori.domain.newsletter.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/newletters")
public class NewsletterController {

    // 발행 연도 조회
    // GET /api/v1/newletters/years

    // 발행호 목록 조회
    // GET /api/v1/newletters

    // 발행호 상세 조회
    // GET /api/v1/newletters/{newsletterId}
}
