package com.daroring.fromdarori.domain.newsletter.service;

import com.daroring.fromdarori.domain.newsletter.dto.NewsletterReservationResponse;
import com.daroring.fromdarori.domain.newsletter.entity.Newsletter;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterSchedule;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import com.daroring.fromdarori.domain.newsletter.repository.NewsletterRepository;
import com.daroring.fromdarori.domain.story.exception.ApiException;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewsletterReservationService {

    private final NewsletterRepository repository;
    private final NewsletterScheduleService scheduleService;

    @Transactional
    public NewsletterReservationResponse reserve(UUID id) {
        Newsletter newsletter = findForUpdate(id);
        if (newsletter.getStatus() != NewsletterStatus.SAVED) {
            throw conflict("NEWSLETTER4091", "저장 상태인 뉴스레터만 예약할 수 있습니다.");
        }
        NewsletterSchedule schedule = scheduleService.getSchedule();
        var now = ZonedDateTime.now(Newsletter.ISSUE_ZONE);
        var month = YearMonth.from(now);
        var candidate = occurrence(month, schedule);
        var scheduledAt = (candidate.isAfter(now) ? candidate : occurrence(month.plusMonths(1), schedule))
                .toOffsetDateTime();
        if (repository.existsByScheduledAt(scheduledAt)) {
            throw occupied();
        }

        newsletter.schedule(scheduledAt);
        try {
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            for (Throwable cause = exception.getCause(); cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException constraint
                        && "newsletter_scheduled_at_key".equals(constraint.getConstraintName())) {
                    throw occupied();
                }
            }
            throw exception;
        }
        return response(newsletter);
    }

    @Transactional
    public NewsletterReservationResponse cancel(UUID id) {
        Newsletter newsletter = findForUpdate(id);
        if (newsletter.getStatus() == NewsletterStatus.SENDING || newsletter.getStatus() == NewsletterStatus.PUBLISHED) {
            throw conflict("NEWSLETTER4094", "이미 발송이 시작되어 예약을 취소할 수 없습니다.");
        }
        if (newsletter.getStatus() != NewsletterStatus.SCHEDULED) {
            throw conflict("NEWSLETTER4093", "예약 상태인 뉴스레터만 취소할 수 있습니다.");
        }
        newsletter.cancelSchedule();
        repository.flush();
        return response(newsletter);
    }

    private Newsletter findForUpdate(UUID id) {
        return repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NEWSLETTER4041", "뉴스레터를 찾을 수 없습니다."));
    }

    private static ZonedDateTime occurrence(YearMonth month, NewsletterSchedule schedule) {
        int day = Math.min(month.lengthOfMonth(), schedule.getDayOfMonth());
        return month.atDay(day).atTime(schedule.getLocalTime()).atZone(Newsletter.ISSUE_ZONE);
    }

    private static NewsletterReservationResponse response(Newsletter newsletter) {
        OffsetDateTime scheduledAt = newsletter.getScheduledAt();
        return new NewsletterReservationResponse(newsletter.getId(), newsletter.getTitle(), newsletter.getStatus(),
                scheduledAt == null ? null : scheduledAt.atZoneSameInstant(Newsletter.ISSUE_ZONE).toOffsetDateTime(),
                newsletter.getUpdatedAt().atZoneSameInstant(Newsletter.ISSUE_ZONE).toOffsetDateTime());
    }

    private static ApiException occupied() {
        return conflict("NEWSLETTER4092", "같은 발행 시각에 다른 뉴스레터가 예약되어 있습니다.");
    }

    private static ApiException conflict(String code, String message) {
        return new ApiException(HttpStatus.CONFLICT, code, message);
    }
}
