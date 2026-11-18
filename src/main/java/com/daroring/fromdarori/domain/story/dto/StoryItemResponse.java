package com.daroring.fromdarori.domain.story.dto;

import com.daroring.fromdarori.domain.story.entity.Story;
import com.daroring.fromdarori.domain.story.entity.StorySourceType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record StoryItemResponse(
        UUID storyId,
        StorySourceType sourceType,
        UUID categoryId,
        String categoryName,
        String title,
        OffsetDateTime createdAt,
        String publicationStatus
) {
    public static StoryItemResponse from(Story story, boolean published) {
        return new StoryItemResponse(
                story.getId(),
                story.getSourceType(),
                story.getCategory() == null
                        ? null
                        : story.getCategory().getId(),
                story.getCategory().getName(),
                story.getTitle(),
                story.getCreatedAt(),
                published ? "PUBLISHED" : "UNPUBLISHED"
        );
    }
}
