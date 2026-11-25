package com.daroring.fromdarori.domain.newsletter.mail;

import java.util.List;

public record NewsletterMailContent(String subject, String textBody, String htmlBody, List<InlineImage> inlineImages) {

    public record InlineImage(String contentId, String contentType, byte[] bytes) {
    }
}
