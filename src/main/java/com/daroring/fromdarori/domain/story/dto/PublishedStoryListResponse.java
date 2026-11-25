package com.daroring.fromdarori.domain.story.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record PublishedStoryListResponse(Integer selectedYear, int page, int size, boolean hasNext, List<Item> items) {

    public record Item(
            UUID publishedStoryId, String categoryName, int issueYear, int issueMonth, String title, String excerpt
    ) {
    }
}
