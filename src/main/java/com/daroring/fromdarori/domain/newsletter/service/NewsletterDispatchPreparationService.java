package com.daroring.fromdarori.domain.newsletter.service;

import com.daroring.fromdarori.domain.newsletter.repository.NewsletterDeliveryRepository;
import com.daroring.fromdarori.domain.newsletter.repository.NewsletterRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsletterDispatchPreparationService {

    private final NewsletterRepository newsletters;
    private final NewsletterDeliveryRepository deliveries;

    @Transactional
    public void prepare(UUID newsletterId, OffsetDateTime now) {
        var newsletter = newsletters.findByIdForUpdate(newsletterId).orElse(null);
        if (newsletter == null || !newsletter.startSendingIfDue(now)) {
            return;
        }

        int count = deliveries.enqueueActiveSubscribers(newsletterId);
        log.debug("Prepared {} delivery jobs for newsletter {}", count, newsletterId);
    }
}
