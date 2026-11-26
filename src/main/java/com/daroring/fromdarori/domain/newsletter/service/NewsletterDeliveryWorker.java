package com.daroring.fromdarori.domain.newsletter.service;

import com.daroring.fromdarori.domain.newsletter.entity.NewsletterDeliveryStatus;
import com.daroring.fromdarori.domain.newsletter.mail.NewsletterEmailRenderer;
import com.daroring.fromdarori.domain.newsletter.mail.NewsletterEmailRenderer.DeliveryLinks;
import com.daroring.fromdarori.domain.newsletter.mail.NewsletterMailContent;
import com.daroring.fromdarori.domain.newsletter.mail.NewsletterMailDraftFactory;
import com.daroring.fromdarori.domain.newsletter.mail.NewsletterMailSender;
import com.daroring.fromdarori.domain.newsletter.mail.NewsletterMailSender.SendFailure;
import com.daroring.fromdarori.domain.newsletter.repository.NewsletterDeliveryRepository;
import com.daroring.fromdarori.domain.newsletter.repository.NewsletterRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsletterDeliveryWorker {

    private final NewsletterDeliveryRepository deliveries;
    private final NewsletterRepository newsletters;
    private final NewsletterMailDraftFactory draftFactory;
    private final NewsletterEmailRenderer renderer;
    private final ObjectProvider<NewsletterMailSender> senders;
    private final TransactionTemplate transactions;

    @Scheduled(fixedDelay = 5000)
    public void process() {
        var sender = senders.getIfAvailable();
        if (sender == null) return;

        var now = now();
        transactions.executeWithoutResult(tx -> deliveries.markInterruptedAttempts(now.minusMinutes(15), now));
        for (UUID id : deliveries.findReadyIds(now, PageRequest.of(0, 20))) {
            try {
                send(id, sender);
            } catch (RuntimeException exception) {
                log.error("Could not process newsletter delivery {}", id, exception);
            }
        }
        transactions.executeWithoutResult(tx -> newsletters.publishAfterFirstAttempts(now()));
    }

    private void send(UUID id, NewsletterMailSender sender) {
        var attempt = transactions.execute(tx -> deliveries.findForUpdate(id).map(delivery -> {
            if (!delivery.startAttempt(now())) return null;
            try {
                return new Attempt(delivery.getAttemptCount(), delivery.getSubscriber().getEmail(),
                        renderer.render(draftFactory.fromSnapshot(delivery.getNewsletter()), DeliveryLinks.none()));
            } catch (RuntimeException exception) {
                delivery.fail(delivery.getAttemptCount(), NewsletterDeliveryStatus.FAILED, "Email rendering failed", null);
                log.warn("Could not render newsletter delivery {}", id);
                return null;
            }
        }).orElse(null));
        if (attempt == null) return;

        String messageId;
        try {
            messageId = sender.send(id, attempt.recipient(), attempt.content());
        } catch (SendFailure failure) {
            var outcome = switch (failure.kind()) {
                case RETRYABLE -> attempt.number() < 3 ? NewsletterDeliveryStatus.RETRY_PENDING : NewsletterDeliveryStatus.FAILED;
                case PERMANENT -> NewsletterDeliveryStatus.FAILED;
                case UNKNOWN -> NewsletterDeliveryStatus.UNKNOWN;
            };
            var retryAt = outcome == NewsletterDeliveryStatus.RETRY_PENDING ? now().plusMinutes(attempt.number()) : null;
            transactions.executeWithoutResult(tx -> deliveries.findForUpdate(id)
                    .ifPresent(delivery -> delivery.fail(attempt.number(), outcome, failure.getMessage(), retryAt)));
            return;
        }
        transactions.executeWithoutResult(tx -> deliveries.findForUpdate(id)
                .ifPresent(delivery -> delivery.accept(attempt.number(), now(), messageId)));
    }

    private static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    private record Attempt(int number, String recipient, NewsletterMailContent content) {
    }
}
