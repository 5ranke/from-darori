package com.daroring.fromdarori.domain.story.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StoryImageResponse(
        @NotNull
        UUID imageId,

        @NotNull
        @Min(value = 1)
        Integer displayOrder
) {
}
