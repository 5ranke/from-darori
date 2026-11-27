package com.daroring.fromdarori.domain.subscriber.dto;

import com.daroring.fromdarori.domain.subscriber.entity.Subscriber;
import com.daroring.fromdarori.domain.subscriber.entity.SubscriberStatus;
import com.daroring.fromdarori.domain.subscriber.entity.Unsubscriber;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UnsubscribeResponse(
        UUID subscriberId,
        String email,
        SubscriberStatus status,
        OffsetDateTime unsubscribedAt
) {
    public static UnsubscribeResponse from(Subscriber subscriber, Unsubscriber unsubscriber) {
        return new UnsubscribeResponse(
                unsubscriber.getSubscriber().getId(),
                subscriber.getEmail(),
                subscriber.getStatus(),
                unsubscriber.getUnsubscribedAt()
        );
    }
}
