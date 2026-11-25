package com.daroring.fromdarori.domain.story.dto;

import com.daroring.fromdarori.domain.story.dto.PublishedStoryContent.Image;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record PublishedStoryDetailResponse(
        UUID publishedStoryId, UUID newsletterId, String categoryName, int issueYear, int issueMonth,
        String title, String body, String actionUrl, String applicationDeadline, List<Image> images
) {
}
