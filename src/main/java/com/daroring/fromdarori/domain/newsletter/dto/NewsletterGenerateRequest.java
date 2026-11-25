package com.daroring.fromdarori.domain.newsletter.dto;

import java.util.List;
import java.util.UUID;

public record NewsletterGenerateRequest(String title, List<UUID> storyIds) {
}
