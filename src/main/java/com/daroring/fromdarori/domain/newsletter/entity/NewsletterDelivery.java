package com.daroring.fromdarori.domain.newsletter.entity;

import com.daroring.fromdarori.domain.subscriber.entity.Subscriber;
import com.daroring.fromdarori.domain.subscriber.entity.SubscriberStatus;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "newsletter_delivery", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"newsletter_id", "subscriber_id"})
})
public class NewsletterDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "newsletter_id", nullable = false)
    private Newsletter newsletter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private Subscriber subscriber;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "provider_message_id", columnDefinition = "text")
    private String providerMessageId;

    @Column(name = "last_attempt_at")
    private OffsetDateTime lastAttemptAt;

    @Column(name = "next_retry_at")
    private OffsetDateTime nextRetryAt;

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private NewsletterDeliveryStatus status = NewsletterDeliveryStatus.PENDING;

    public boolean startAttempt(OffsetDateTime now) {
        if (status != NewsletterDeliveryStatus.PENDING
                && (status != NewsletterDeliveryStatus.RETRY_PENDING || nextRetryAt.isAfter(now))) {
            return false;
        }
        if (subscriber.getStatus() != SubscriberStatus.ACTIVE) {
            status = NewsletterDeliveryStatus.SKIPPED;
            nextRetryAt = null;
            return false;
        }
        status = NewsletterDeliveryStatus.SENDING;
        attemptCount++;
        lastAttemptAt = now;
        nextRetryAt = null;
        return true;
    }

    public void accept(int attempt, OffsetDateTime now, String messageId) {
        if (!isCurrentAttempt(attempt)) return;
        status = NewsletterDeliveryStatus.ACCEPTED;
        acceptedAt = now;
        providerMessageId = messageId;
        failureReason = null;
    }

    public void fail(int attempt, NewsletterDeliveryStatus outcome, String reason, OffsetDateTime retryAt) {
        if (!isCurrentAttempt(attempt)) return;
        status = outcome;
        failureReason = reason;
        nextRetryAt = retryAt;
    }

    private boolean isCurrentAttempt(int attempt) {
        return attemptCount == attempt
                && (status == NewsletterDeliveryStatus.SENDING || status == NewsletterDeliveryStatus.UNKNOWN);
    }
}
