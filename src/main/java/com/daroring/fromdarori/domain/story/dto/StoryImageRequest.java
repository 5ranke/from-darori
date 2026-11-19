package com.daroring.fromdarori.domain.story.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StoryImageRequest(
        @NotBlank String objectKey,
        @NotNull @Positive Integer displayOrder
) {
}
