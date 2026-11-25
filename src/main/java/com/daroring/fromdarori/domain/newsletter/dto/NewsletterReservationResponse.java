package com.daroring.fromdarori.domain.newsletter.dto;

import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record NewsletterReservationResponse(
        UUID newsletterId, String title, NewsletterStatus status, OffsetDateTime scheduledAt, OffsetDateTime updatedAt
) {
}
