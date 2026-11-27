package com.daroring.fromdarori.domain.subscriber.dto;

import java.util.List;

public record SubscriberInfoResponse(
        List<SubscriberItemResponse> subscribers,
        Integer page,
        Integer size,
        Integer totalElements,
        Integer totalPages,
        Boolean hasNext
) {
}
