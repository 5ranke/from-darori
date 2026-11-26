package com.daroring.fromdarori.domain.newsletter.service;

import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import com.daroring.fromdarori.domain.newsletter.repository.NewsletterRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsletterReservationScheduler {

    private final NewsletterRepository repository;
    private final NewsletterDispatchPreparationService preparationService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void checkDueReservations() {
        var now = OffsetDateTime.now(ZoneOffset.UTC);
        for (var newsletterId : repository.findDueNewsletterIds(NewsletterStatus.SCHEDULED, now)) {
            try {
                preparationService.prepare(newsletterId, now);
            } catch (RuntimeException exception) {
                log.error("Could not prepare deliveries for newsletter {}", newsletterId, exception);
            }
        }
    }
}
