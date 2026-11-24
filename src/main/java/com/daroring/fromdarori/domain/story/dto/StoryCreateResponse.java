package com.daroring.fromdarori.domain.story.dto;

import com.daroring.fromdarori.domain.residentreport.dto.ReportToStoryResponse;
import com.daroring.fromdarori.domain.story.entity.Story;
import com.daroring.fromdarori.domain.story.entity.StorySourceType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record StoryCreateResponse(
        UUID storyId,
        StorySourceType sourceType,
        UUID residentReportId,
        UUID categoryId,
        String title,
        UUID createdBy,
        OffsetDateTime createdAt,
        List<StoryImageResponse> images
) {
    public static StoryCreateResponse from(Story story, List<StoryImageResponse> images) {
        return new StoryCreateResponse(
                story.getId(),
                story.getSourceType(),
                story.getResidentReport() == null ? null : story.getResidentReport().getId(),
                story.getCategory().getId(),
                story.getTitle(),
                story.getCreatedBy() == null ? null : story.getCreatedBy().getId(),
                story.getCreatedAt(),
                images
        );
    }
}
