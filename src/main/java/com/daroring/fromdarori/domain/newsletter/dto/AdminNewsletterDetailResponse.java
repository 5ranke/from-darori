package com.daroring.fromdarori.domain.newsletter.dto;

import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record AdminNewsletterDetailResponse(
        UUID newsletterId, String title, String summary, NewsletterStatus status, String intro,
        List<Story> stories, String closing, JsonNode quiz,
        OffsetDateTime createdAt, OffsetDateTime scheduledAt, OffsetDateTime publishedAt
) {
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Story(
            UUID storyId, int displayOrder, String categoryName, String title, String body,
            String actionUrl, String applicationDeadline, List<JsonNode> images
    ) {
    }
}
