package com.daroring.fromdarori.domain.newsletter.dto;

import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AdminNewsletterListResponse(
        long totalCount, List<Item> newsletters, int page, int size, int totalPages, boolean hasNext
) {
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Item(UUID newsletterId, Long issueNumber, String title, NewsletterStatus status, OffsetDateTime createdAt) {
    }
}
