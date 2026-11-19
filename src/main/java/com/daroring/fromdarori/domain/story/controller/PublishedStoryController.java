package com.daroring.fromdarori.domain.story.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/published-stories")
public class PublishedStoryController {

    // 카테고리별 수록 소식 목록 조회
    // GET /api/v1/published-stories

    // 수록 소식 상세 조회
    // GET /api/v1/published-stories/{publishedStoryId}
}
