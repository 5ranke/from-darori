package com.daroring.fromdarori.domain.newsletter.dto;

import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record NewsletterSaveResponse(
        UUID newsletterId, String title, NewsletterStatus status, int storyCount, UUID createdBy, OffsetDateTime createdAt
) {
}
