package com.daroring.fromdarori.domain.story.dto;

import com.daroring.fromdarori.domain.story.entity.Story;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record StoryUpdateResponse (
        UUID storyId,
        String title,
        UUID categoryId,
        OffsetDateTime updatedAt,
        List<StoryImageResponse> images
) {
    public static StoryUpdateResponse from(Story story, List<StoryImageResponse> images) {
        return new StoryUpdateResponse(
                story.getId(),
                story.getTitle(),
                story.getCategory().getId(),
                story.getUpdatedAt(),
                images
        );
    }
}
