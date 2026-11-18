package com.daroring.fromdarori.domain.story.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StoryImageRequest(
        @NotNull
        String objectKey,

        @NotNull
        @Min(value = 1)
        Integer displayOrder
) {
}
