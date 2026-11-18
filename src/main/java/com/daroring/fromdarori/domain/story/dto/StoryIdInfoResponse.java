package com.daroring.fromdarori.domain.story.dto;

import com.daroring.fromdarori.domain.story.entity.Story;
import com.daroring.fromdarori.domain.story.entity.StorySourceType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record StoryIdInfoResponse(
        UUID storyId,
        StorySourceType sourceType,
        UUID residentReportId,
        UUID categoryId,
        String categoryName,
        String title,
        String body,
        String actionUrl,
        LocalDate applicationDeadline,
        UUID createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<StoryIdImageResponse> images
) {
    public static StoryIdInfoResponse from(Story story, List<StoryIdImageResponse> images) {
        return new StoryIdInfoResponse(
                story.getId(),
                story.getSourceType(),
                story.getResidentReport() == null ? null : story.getResidentReport().getId(),
                story.getCategory().getId(),
                story.getCategory().getName(),
                story.getTitle(),
                story.getBody(),
                story.getActionUrl(),
                story.getApplicationDeadline(),
                story.getCreatedBy().getId(),
                story.getCreatedAt(),
                story.getUpdatedAt(),
                images
        );
    }
}
