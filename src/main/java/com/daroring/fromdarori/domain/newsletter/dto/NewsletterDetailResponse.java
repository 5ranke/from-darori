package com.daroring.fromdarori.domain.newsletter.dto;

import com.daroring.fromdarori.domain.story.dto.PublishedStoryContent;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record NewsletterDetailResponse(
        UUID newsletterId, int issueYear, int issueMonth, String title, String summary, boolean isLatest,
        List<Section> sections, Quiz quiz
) {
    public record Section(String title, List<PublishedStoryContent> stories) {
    }

    public record Quiz(String question, List<String> choices) {
    }
}
