package com.daroring.fromdarori.domain.newsletter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record NewsletterDetailResponse(
        UUID newsletterId, int issueYear, int issueMonth, String title, String summary, boolean isLatest,
        List<Section> sections, Quiz quiz
) {
    public record Section(String title, List<Story> stories) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Story(String title, String body, String actionUrl, String applicationDeadline, List<Image> images) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Image(String url, String altText) {
    }

    public record Quiz(String question, List<String> choices) {
    }
}
