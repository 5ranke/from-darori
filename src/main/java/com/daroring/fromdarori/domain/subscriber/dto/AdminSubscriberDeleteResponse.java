package com.daroring.fromdarori.domain.subscriber.dto;

import com.daroring.fromdarori.domain.subscriber.entity.SubscriberStatus;

import java.util.UUID;

public record AdminSubscriberDeleteResponse(
        UUID subscriberId,
        SubscriberStatus status
) {
}
