package com.daroring.fromdarori.domain.residentreport.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/resident-reports")
public class AdminResidentReportController {

    // 받은 제보 목록 조회
    // GET /api/v1/admin/resident-reports

    // 제보 상세 조회
    // GET /api/v1/admin/resident-reports/{reportId}

    // 제보를 소식으로 등록
    // POST /api/v1/admin/resident-reports/{reportId}/story

    // 제보 삭제
    // DELETE /api/v1/admin/resident-reports/{reportId}
}
