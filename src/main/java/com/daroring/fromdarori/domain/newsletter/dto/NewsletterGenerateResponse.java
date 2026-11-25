package com.daroring.fromdarori.domain.newsletter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record NewsletterGenerateResponse(
        String title, String summary, String intro, List<StoryDraft> stories, String closing, Quiz quiz
) {
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record StoryDraft(
            UUID storyId, int displayOrder, UUID categoryId, String title, String body, String categoryName,
            String actionUrl, LocalDate applicationDeadline, List<Image> images
    ) {
    }

    public record Image(String objectKey, String imageUrl, int displayOrder) {
    }

    public record Quiz(String question, List<String> options, int answer, UUID storyId, String explanation, String evidence) {
    }
}
