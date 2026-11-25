package com.daroring.fromdarori.domain.newsletter.service;

import com.daroring.fromdarori.domain.newsletter.entity.NewsletterSchedule;
import com.daroring.fromdarori.domain.newsletter.repository.NewsletterScheduleRepository;
import com.daroring.fromdarori.domain.story.exception.ApiException;
import java.time.LocalTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NewsletterScheduleService {

    private final NewsletterScheduleRepository repository;

    @Transactional(readOnly = true)
    public NewsletterSchedule getSchedule() {
        return repository.findBySingletonTrue()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SCHEDULE4041", "발행 주기 설정이 없습니다."));
    }

    @Transactional
    public NewsletterSchedule save(Integer dayOfMonth, String localTime, UUID adminId) {
        if (dayOfMonth == null || dayOfMonth < 1 || dayOfMonth > 31 || localTime == null
                || !localTime.matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "발행일은 1~31일, 발행 시각은 HH:mm:ss 형식으로 입력해 주세요.");
        }
        repository.upsert(dayOfMonth.shortValue(), LocalTime.parse(localTime), adminId);
        return getSchedule();
    }
}
