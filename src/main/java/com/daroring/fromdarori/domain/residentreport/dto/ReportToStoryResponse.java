package com.daroring.fromdarori.domain.residentreport.dto;

import com.daroring.fromdarori.domain.residentreport.entity.ResidentReport;
import com.daroring.fromdarori.domain.residentreport.entity.ResidentReportStatus;
import com.daroring.fromdarori.domain.story.dto.StoryImageResponse;
import com.daroring.fromdarori.domain.story.entity.Story;
import com.daroring.fromdarori.domain.story.entity.StorySourceType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ReportToStoryResponse(
        UUID storyId,
        UUID residentReportId,
        StorySourceType sourceType,
        String title,
        UUID categoryId,
        ResidentReportStatus status,
        UUID approvedBy,
        OffsetDateTime approvedAt,
        OffsetDateTime createdAt,
        List<StoryImageResponse> images
) {
    public static ReportToStoryResponse from(Story story, ResidentReport report, List<StoryImageResponse> images) {
        return new ReportToStoryResponse(
                story.getId(),
                report.getId(),
                story.getSourceType(),
                story.getTitle(),
                story.getCategory().getId(),
                report.getStatus(),
                report.getApprovedBy().getId(),
                report.getApprovedAt(),
                story.getCreatedAt(),
                images
        );
    }
}
