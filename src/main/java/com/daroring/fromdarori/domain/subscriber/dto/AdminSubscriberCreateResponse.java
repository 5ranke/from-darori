package com.daroring.fromdarori.domain.subscriber.dto;

import com.daroring.fromdarori.domain.subscriber.entity.Subscriber;
import com.daroring.fromdarori.domain.subscriber.entity.SubscriberStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminSubscriberCreateResponse(
        UUID subscriberId,
        String name,
        String email,
        SubscriberStatus status,
        OffsetDateTime createdAt
) {
    public static AdminSubscriberCreateResponse from(Subscriber subscriber) {
        return new AdminSubscriberCreateResponse(
                subscriber.getId(),
                subscriber.getName(),
                subscriber.getEmail(),
                subscriber.getStatus(),
                subscriber.getCreatedAt()
        );
    }
}
