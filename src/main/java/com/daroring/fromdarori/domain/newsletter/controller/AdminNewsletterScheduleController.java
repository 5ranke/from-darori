package com.daroring.fromdarori.domain.newsletter.controller;

import com.daroring.fromdarori.domain.newsletter.entity.Newsletter;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterSchedule;
import com.daroring.fromdarori.domain.newsletter.service.NewsletterScheduleService;
import com.daroring.fromdarori.global.response.ApiResponse;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/newsletter-schedule")
public class AdminNewsletterScheduleController {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final NewsletterScheduleService scheduleService;

    @GetMapping
    public ApiResponse<ScheduleResponse> getSchedule() {
        return ApiResponse.success("발행 주기를 조회했습니다.", toResponse(scheduleService.getSchedule()));
    }

    @PostMapping
    public ApiResponse<ScheduleSaveResponse> save(
            @RequestBody(required = false) ScheduleRequest request, @AuthenticationPrincipal Jwt principal
    ) {
        UUID adminId = UUID.fromString(principal.getSubject());
        NewsletterSchedule schedule = scheduleService.save(request == null ? null : request.dayOfMonth(),
                request == null ? null : request.localTime(), adminId);
        var response = toResponse(schedule);
        return ApiResponse.success("발행 주기가 저장되었습니다.", new ScheduleSaveResponse(response.scheduleId(),
                response.dayOfMonth(), response.localTime(), response.timezone(), adminId, response.updatedAt()));
    }

    private static ScheduleResponse toResponse(NewsletterSchedule schedule) {
        var zone = Newsletter.ISSUE_ZONE;
        return new ScheduleResponse(schedule.getId(), schedule.getDayOfMonth(),
                schedule.getLocalTime().format(TIME_FORMAT), zone.getId(),
                schedule.getUpdatedAt().atZoneSameInstant(zone).toOffsetDateTime());
    }

    public record ScheduleRequest(Integer dayOfMonth, String localTime) {
    }

    public record ScheduleResponse(UUID scheduleId, int dayOfMonth, String localTime, String timezone,
                                   OffsetDateTime updatedAt) {
    }

    public record ScheduleSaveResponse(UUID scheduleId, int dayOfMonth, String localTime, String timezone,
                                       UUID updatedBy, OffsetDateTime updatedAt) {
    }
}
