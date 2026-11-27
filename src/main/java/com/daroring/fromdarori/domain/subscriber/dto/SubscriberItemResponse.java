package com.daroring.fromdarori.domain.subscriber.dto;

import com.daroring.fromdarori.domain.subscriber.entity.Subscriber;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SubscriberItemResponse(
        UUID subscriberId,
        String name,
        String email,
        OffsetDateTime createdAt
) {
    public static SubscriberItemResponse from(Subscriber subscriber) {
        return new SubscriberItemResponse(
                subscriber.getId(),
                subscriber.getName(),
                subscriber.getEmail(),
                subscriber.getCreatedAt()
        );
    }
}
