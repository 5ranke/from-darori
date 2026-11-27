package com.daroring.fromdarori.domain.subscriber.dto;

import com.daroring.fromdarori.domain.subscriber.entity.Subscriber;
import com.daroring.fromdarori.domain.subscriber.entity.SubscriberStatus;

import java.util.UUID;

public record SubscribeCreateResponse(
        UUID subscriberId,
        String email,
        SubscriberStatus status
) {
    public static SubscribeCreateResponse from(Subscriber subscriber) {
        return new SubscribeCreateResponse(
                subscriber.getId(),
                subscriber.getEmail(),
                subscriber.getStatus()
        );
    }
}
