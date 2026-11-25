package com.daroring.fromdarori.domain.home.controller;

import com.daroring.fromdarori.domain.home.dto.HomeResponse;
import com.daroring.fromdarori.domain.home.service.HomeService;
import com.daroring.fromdarori.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public ApiResponse<HomeResponse> getHome() {
        return ApiResponse.success("홈 화면 조회에 성공했습니다.", homeService.getHome());
    }
}
