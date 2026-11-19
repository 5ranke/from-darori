package com.daroring.fromdarori.domain.story.dto;

import java.util.List;

public record StoryInfoResponse(
    Integer totalCount,
    Integer page,
    Integer size,
    Integer totalPages,
    Boolean hasNext,
    List<StoryItemResponse> stories
) {
}
