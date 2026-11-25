package com.daroring.fromdarori.domain.newsletter.mail;

import java.time.LocalDate;
import java.util.List;

public record NewsletterMailDraft(
        String issue, String title, String summary, String intro, List<Story> stories, String closing, Quiz quiz
) {
    public record Story(
            int displayOrder, String categoryName, String title, String body, String actionUrl,
            LocalDate applicationDeadline, String sourceName, String sourceUrl, String purchaseInfo, List<Image> images
    ) {
    }

    public record Image(String objectKey, String imageUrl, String altText, int displayOrder) {
    }

    public record Quiz(String question, List<String> options) {
    }
}
