package com.daroring.fromdarori.domain.story.dto;

import java.util.UUID;

public record StoryIdImageResponse(
        UUID imageId,
        String imageUrl,
        Integer displayOrder
) {
}
